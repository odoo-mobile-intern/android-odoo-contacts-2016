package com.odoo.orm.sync.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.odoo.orm.OModel;

public class ContactProvider extends ContentProvider {
    public static final String TAG = ContactProvider.class.getSimpleName();
    public OModel oModel = null;

    @Override
    public boolean onCreate() {
        return true;
    }

    public void createDBObject(Uri uri) {
        oModel = new OModel(getContext(), "res_partner") {
        };
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        createDBObject(uri);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor cr;
        queryBuilder.setTables("res_partner");

        cr = queryBuilder.query(oModel.getReadableDatabase(), projection,
                selection, selectionArgs, null, null, sortOrder);
        Context ctx = getContext();
        assert ctx != null;
        assert cr != null;
        cr.setNotificationUri(ctx.getContentResolver(), uri);
        return cr;
    }

    @Override
    public String getType(Uri uri) {
        return uri.toString();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

}
