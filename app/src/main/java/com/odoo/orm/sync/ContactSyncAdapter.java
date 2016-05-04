package com.odoo.orm.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.table.ResPartner;

import java.util.ArrayList;
import java.util.List;

import odoo.Odoo;
import odoo.handler.OdooVersionException;
import odoo.helper.ODomain;
import odoo.helper.OUser;
import odoo.helper.OdooFields;
import odoo.helper.utils.gson.OdooRecord;
import odoo.helper.utils.gson.OdooResult;

public class ContactSyncAdapter extends AbstractThreadedSyncAdapter {

    private Context mContext;
    private Odoo odoo;
    private OUser mUser;
    private AccountManager accountManager;

    public ContactSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        // Finding details of user by account and account manager
        mUser = findUser(account);
        try {

            // Because onPerformSync works in background. we have to use synchronized method in this scope.
            Odoo.DEBUG=true;
            // Quick connecting with odoo in synchronized mode
            odoo = Odoo.createQuickInstance(mContext, mUser.getHost());
            // Quick authenticating with user in synchronized mode
            mUser = odoo.authenticate(mUser.getUsername(), mUser.getPassword(), mUser.getDatabase());

            // Creating Respartner database table object
            ResPartner partner = new ResPartner(mContext);

            OdooFields fields = new OdooFields();
            fields.addAll(partner.getServerColumn());
            ODomain domain = new ODomain();

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
            Log.v("Sync Finished", recordIds.toString() + " records created/updated");
        } catch (OdooVersionException e) {
            e.printStackTrace();
        }
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
