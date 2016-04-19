package com.odoo.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by sha on 19/4/16.
 */
public class OdooAuthenticatorServices extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new OdooAuthenticator(getApplicationContext()).getIBinder();
    }
}
