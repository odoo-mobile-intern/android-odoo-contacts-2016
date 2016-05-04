package com.odoo.orm.sync.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.odoo.orm.OModel;
import com.odoo.table.ResPartner;

public class ContactProvider extends ContentProvider {
    public static final String TAG = ContactProvider.class.getSimpleName();
    public OModel oModel = null;
    private final int COLLECTION = 1;
    private final int SINGLE_ROW = 2;
    public UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static Uri buildURI(String authority, String table_name) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.appendPath(table_name);
        uriBuilder.appendQueryParameter("table_name", table_name);
        uriBuilder.scheme("content");
        return uriBuilder.build();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    public void setMatcher(Uri uri) {
        String AUTHORITY = ResPartner.AUTHORITY;
        matcher.addURI(AUTHORITY, oModel.getTableName(), COLLECTION);
        matcher.addURI(AUTHORITY, oModel.getTableName() + "/#", SINGLE_ROW);
    }

    public void createDBObject(Uri uri) {
        oModel = new OModel(getContext(), uri.getQueryParameter("table_name")) {
        };
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        createDBObject(uri);
        setMatcher(uri);
        int match = matcher.match(uri);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor cr = null;
        queryBuilder.setTables(oModel.getTableName());
        switch (match) {
            case COLLECTION:
                cr = queryBuilder.query(oModel.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_ROW:
                String row_id = uri.getLastPathSegment();
                cr = queryBuilder.query(oModel.getReadableDatabase(), projection,
                        "_id = ?", new String[]{row_id}, null, null, null);
                break;
            case UriMatcher.NO_MATCH:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
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
        createDBObject(uri);
        setMatcher(uri);
        int match = matcher.match(uri);
        Uri newUri = null;
        switch (match) {
            case COLLECTION:
                SQLiteDatabase db = oModel.getWritableDatabase();
                long new_id = db.insert(oModel.getTableName(), null, values);
                newUri = Uri.withAppendedPath(uri, new_id + "");
                break;
            case SINGLE_ROW:
                throw new UnsupportedOperationException(
                        "Insert not supported on URI: " + uri);
            case UriMatcher.NO_MATCH:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        assert newUri != null;
        notifyDataChange(newUri);
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        createDBObject(uri);
        setMatcher(uri);
        int match = matcher.match(uri);
        int count = 0;
        switch (match) {
            case COLLECTION:
                SQLiteDatabase db = oModel.getWritableDatabase();
                count = db.delete(oModel.getTableName(), selection, selectionArgs);
                break;
            case SINGLE_ROW:
                db = oModel.getWritableDatabase();
                String row_id = uri.getLastPathSegment();
                count = db.delete(oModel.getTableName(), "_id = ?",
                        new String[]{row_id});
                break;
            case UriMatcher.NO_MATCH:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        notifyDataChange(uri);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        createDBObject(uri);
        setMatcher(uri);
        int count = 0;
        int match = matcher.match(uri);
        switch (match) {
            case COLLECTION:
                SQLiteDatabase db = oModel.getWritableDatabase();
                count = db.update(oModel.getTableName(), values, selection,
                        selectionArgs);
                break;
            case SINGLE_ROW:
                db = oModel.getWritableDatabase();
                String row_id = uri.getLastPathSegment();
                count = db.update(oModel.getTableName(), values, "_id = ?",
                        new String[]{row_id});
                break;
            case UriMatcher.NO_MATCH:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        notifyDataChange(uri);
        return count;
    }

    private void notifyDataChange(Uri uri) {
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null);
    }
}
