package com.odoo.orm.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

public class ContactSyncAdatper extends AbstractThreadedSyncAdapter {

    public ContactSyncAdatper(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        //TODO: Logic for sync data.....
        // Getting data from server.and storing data to database....
    }
}
