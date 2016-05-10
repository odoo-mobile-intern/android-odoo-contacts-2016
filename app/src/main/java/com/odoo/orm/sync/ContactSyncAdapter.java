package com.odoo.orm.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.orm.ListRow;
import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.ColumnType;
import com.odoo.table.ResPartner;
import com.odoo.utils.ODateUtils;

import java.util.ArrayList;
import java.util.List;

import odoo.Odoo;
import odoo.handler.OdooVersionException;
import odoo.helper.ODomain;
import odoo.helper.ORecordValues;
import odoo.helper.OUser;
import odoo.helper.OdooFields;
import odoo.helper.utils.gson.OdooRecord;
import odoo.helper.utils.gson.OdooResult;

public class ContactSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String KEY_LAST_SYNC_DATETIME = "last_sync_datetime";
    public static final String AUTHORITY = "com.odoo.contacts.res_partner";
    private Context mContext;
    private Odoo odoo;
    private OUser mUser;
    private SharedPreferences pref;
    private AccountManager accountManager;

    public ContactSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        pref = context.getSharedPreferences("sync_meta", Context.MODE_PRIVATE);
        accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        // Finding details of user by account and account manager
        mUser = findUser(account);
        try {

            // Because onPerformSync works in background. we have to use synchronized method in this scope.
            // Quick connecting with odoo in synchronized mode
            odoo = Odoo.createQuickInstance(mContext, mUser.getHost());
            // Quick authenticating with user in synchronized mode
            mUser = odoo.authenticate(mUser.getUsername(), mUser.getPassword(), mUser.getDatabase());

            // Creating Respartner database table object
            ResPartner partner = new ResPartner(mContext);

            /**
             * Creating record got from server also updating if record is newer than local
             */
            List<Integer> recordIds = createOrUpdateRecords(partner);
            Log.v("Create Or Update", recordIds + " records affected locally");


            /**
             * Creating record on server if any local record with zero (0) id
             */
            List<Integer> createdIds = createRecordsOnServer(partner);
            Log.v("Creating on server", createdIds + " records created on server");

            /**
             * Storing last sync date and time to preferences.
             */
            String lastSyncOn = ODateUtils.getCurrentDateTime();
            Log.v("Sync Finished", "Sync finished on " + lastSyncOn);
//            Log.v("Sync Finished:", recordIds.toString() + " records created/updated =>>" + lastSyncOn);

            pref.edit().putString(KEY_LAST_SYNC_DATETIME, lastSyncOn).apply();
        } catch (OdooVersionException e) {
            e.printStackTrace();
        }
    }


    private List<Integer> createOrUpdateRecords(ResPartner partner) {
        OdooFields fields = new OdooFields();
        fields.addAll(partner.getServerColumn());
        ODomain domain = new ODomain();

        /**
         * Adding create_date compare with last sync date.
         */
        String lastSyncDatetime = pref.getString(KEY_LAST_SYNC_DATETIME, null);
        if (lastSyncDatetime != null) {
            String utcLastSyncDate = ODateUtils.convertToUTC(lastSyncDatetime,
                    ODateUtils.DEFAULT_FORMAT);

            domain.add("|");
            domain.add("create_date", ">=", utcLastSyncDate);
            domain.add("write_date", ">=", utcLastSyncDate);
        }

        // getting records from odoo server.
        OdooResult result = odoo.searchRead(partner.getModelName(), fields, domain, 0, 0, null);

        List<Integer> recordIds = new ArrayList<>();

        // filtering each of the record with the column values.
        for (OdooRecord record : result.getRecords()) {
            ContentValues values = new ContentValues();

            // looping with each column and setting the content value as per its type.
            for (OColumn column : partner.getColumns()) {
                if (!column.isLocal) {
                    switch (column.columnType) {
                        case VARCHAR:
                            values.put(column.name, record.getString(column.name));
                            break;
                        case BOOLEAN:
                            values.put(column.name, record.getBoolean(column.name));
                            break;
                        case BLOB:
                            values.put(column.name, record.getString(column.name));
                            break;
                        case INTEGER:
                            values.put(column.name, record.getInt(column.name));
                            break;
                        case DATETIME:
                            values.put(column.name, record.getString(column.name));
                            break;
                        case MANY2ONE:

                            // Here many to one record refers another table.
                            // so creating primary record for that table and adding
                            // primary key field unique id to reference value

                            OdooRecord m2oRecord = record.getM20(column.name);
                            int m2oId = 0;
                            if (m2oRecord != null) {
                                String modelName = column.relModel;
                                OModel model = OModel.createInstance(modelName, mContext);
                                if (model != null) {
                                    ContentValues m2oValues = new ContentValues();
                                    m2oValues.put("id", m2oRecord.getInt("id"));
                                    m2oValues.put("name", m2oRecord.getString("name"));
                                    m2oId = model.update_or_create(m2oValues, "id = ?",
                                            m2oRecord.getInt("id") + "");
                                }
                            }
                            values.put(column.name, m2oId);
                            break;
                    }
                }
            }
            // Creating or updating record in the database..
            int record_id = partner.update_or_create(values, "id = ?", record.getInt("id") + "");
            recordIds.add(record_id);
        }
        return recordIds;
    }


    private List<Integer> createRecordsOnServer(ResPartner partner) {
        List<Integer> ids = new ArrayList<>();
        for (ListRow row : partner.select("id = ?", "0")) {
            ORecordValues values = new ORecordValues();

            for (OColumn column : partner.getColumns()) {
                if (!column.isLocal && !column.name.equals("id")) {
                    Object value = row.get(column.name);
                    if (!value.toString().equals("false") ||
                            column.columnType == ColumnType.BOOLEAN) {
                        switch (column.columnType) {
                            case MANY2ONE:
                                //TODO: To be implemented.
                                continue;
                        }
                        values.put(column.name, value);
                    }
                }
            }

            // Creating record on server
            OdooResult result = odoo.createRecord(partner.getModelName(), values);
            int newServerId = result.getInt("result");
            ids.add(newServerId);
            // Updating local record with new created server id
            ContentValues newValues = new ContentValues();
            newValues.put("id", newServerId);
            partner.update(newValues, "_id = ?", row.getString("_id"));
        }
        return ids;
    }

    /**
     * creates the detail for user of account. for connecting with odoo
     *
     * @param account object of device account
     * @return object of odoo user
     */
    private OUser findUser(Account account) {
        OUser user = new OUser();
        user.setHost(accountManager.getUserData(account, "host"));
        user.setUsername(accountManager.getUserData(account, "username"));
        user.setPassword(accountManager.getPassword(account));
        user.setDatabase(accountManager.getUserData(account, "database"));
        return user;
    }

}
