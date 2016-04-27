package com.odoo.orm;

import android.database.Cursor;

import java.util.HashMap;

/**
 * Created by sha on 25/4/16.
 */
public class ListRow extends HashMap<String, Object> {

    public ListRow() {

    }

    public ListRow(Cursor cursor) {
        for (String col : cursor.getColumnNames()) {
            int index = cursor.getColumnIndex(col);
            switch (cursor.getType(index)) {
                case Cursor.FIELD_TYPE_STRING:
                    put(col, cursor.getString(index));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    put(col, cursor.getInt(index));
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    put(col, cursor.getBlob(index));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    put(col, cursor.getFloat(index));
            }

        }
    }

    public int getInt(String key) {
        return containsKey(key) ? Integer.parseInt(get(key) + "") : -1;

    }

    public String getString(String key) {
        return containsKey(key) ? get(key) + " " : "false";
    }

}
