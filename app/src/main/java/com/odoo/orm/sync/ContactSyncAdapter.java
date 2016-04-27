package com.odoo.orm.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class ContactSyncAdapter extends AbstractThreadedSyncAdapter {

    public ContactSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        //TODO: Logic for sync data.....
        // Getting data from server.and storing data to database....
        for (int i = 1; i <= 500; i++) {
            Log.v("Hello", "I'm synceddd " + i);
        }
    }
}
