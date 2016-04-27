package com.odoo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.odoo.table.AllTables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sha on 25/4/16.
 */
public class ContactDatabase extends SQLiteOpenHelper implements BaseColumns {
    public static final String DB_NAME = "CONTACTDATABASE.db";
    public static final int DB_VERSION = 1;
    private Context mcontext;
    private String mtableName;
    private HashMap<String, ContactColumn> mHashMap = new HashMap();

    public ContactDatabase(Context context, String tableName) {
        super(context, DB_NAME, null, DB_VERSION);
        mcontext = context;
        mtableName = tableName;
        mHashMap.put(_ID, new ContactColumn(_ID, ColumnType.INTEGER).makePrimaryKey().makeautoIncreament());


    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        HashMap<String, ContactDatabase> map = new AllTables().tables(mcontext);
        for (ContactDatabase table : map.values()) {
            StatementBuilder ContactBuilder = new StatementBuilder(table);
            String sql = ContactBuilder.createStatement();
            if (sql != null) {
                db.execSQL(sql);
                Log.v("Database", "Table created : " + table.getTableName());
            }

        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String getTableName() {
        return mtableName.replace(".","_");
    }


    public ContactDatabase AddColumns(ContactColumn column) {
        mHashMap.put(column.FieldName, column);
        return this;
    }

    public HashMap<String, ContactColumn> getColumn() {
        return mHashMap;
    }

    public List<ListRow> select() {
        return select(null);
    }

    public int create(ContentValues contentValues) {
        SQLiteDatabase database = getWritableDatabase();
        Long id = database.insert(getTableName(), null, contentValues);
        int new_id = id.intValue();
        database.close();
        return new_id;
    }

    public List<ListRow> select(String where, String... args) {
        List<ListRow> rows = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        args = args.length > 0 ? args : null;
        Cursor cursor = db.query(getTableName(), null, where, args, null, null, "_id DECS");
        if (cursor.moveToFirst()) {

            do {
                rows.add(new ListRow(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return rows;

    }

    public int count() {
        int count = 0;
        Cursor cr = null;
        SQLiteDatabase db = getReadableDatabase();

        cr = db.rawQuery("select count(*) as total from " + getTableName(), null);

        if (cr.moveToFirst()) {
            count = cr.getColumnIndex("total");
        }

        cr.close();
        db.close();

        return count;
    }
}
