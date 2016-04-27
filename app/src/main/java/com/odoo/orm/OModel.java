package com.odoo.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.odoo.orm.types.ColumnType;
import com.odoo.table.ModelRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sha on 25/4/16.
 */
public abstract class OModel extends SQLiteOpenHelper implements BaseColumns {
    public static final String DB_NAME = "OdooContacts.db";
    public static final int DB_VERSION = 1;
    private Context mContext;
    private String mModelName;

    OColumn _id = new OColumn("Local ID", ColumnType.INTEGER)
            .makeAutoIncrement()
            .makePrimaryKey();
    OColumn id = new OColumn("Server ID", ColumnType.INTEGER)
            .setDefault("0");

    OColumn _write_date = new OColumn("Local Write date", ColumnType.DATETIME)
            .setDefault("false");

    OColumn is_dirty = new OColumn("Dirty record", ColumnType.BOOLEAN).setDefault("false");


    public OModel(Context context, String model) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
        mModelName = model;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        HashMap<String, OModel> map = new ModelRegistry().models(mContext);
        for (OModel model : map.values()) {
            StatementBuilder sqlBuilder = new StatementBuilder(model);
            String sql = sqlBuilder.createStatement();
            if (sql != null) {
                db.execSQL(sql);
                Log.v("Database", "Model registered : " + model.getModelName());
            }
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String getTableName() {
        return mModelName.replace(".", "_");
    }

    public String getModelName() {
        return mModelName;
    }

    public List<OColumn> getColumn() {
        List<OColumn> columns = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(getClass().getSuperclass().getDeclaredFields()));
        fields.addAll(Arrays.asList(getClass().getDeclaredFields()));

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType().isAssignableFrom(OColumn.class)) {
                try {
                    OColumn column = (OColumn) field.get(this);
                    column.name = field.getName();
                    columns.add(column);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return columns;
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
        Cursor cursor = db.query(getTableName(), null, where, args, null, null, "_id DESC");
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
