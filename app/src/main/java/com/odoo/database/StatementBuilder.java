package com.odoo.database;

import android.util.Log;

/**
 * Created by sha on 25/4/16.
 */
public class StatementBuilder {
    private ContactDatabase mTable;

    public StatementBuilder(ContactDatabase table) {
        this.mTable = table;
    }

    public String createStatement() {

        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE IF NOT EXISTS ")
                .append(mTable.getTableName())
                .append(" (");

        StringBuffer columns = new StringBuffer();
        for (ContactColumn column : mTable.getColumn().values()) {

            columns.append(column.FieldName)
                    .append(" ")
                    .append(column.columnType.toString());

            if (column.primaryKey) {
                columns.append(" PRIMARY KEY ");
            }
            if (column.autoIncreament) {
                columns.append(" AUTOINCREMENT ");
            }
            columns.append(" , ");

        }
        String ColunmString = columns.toString();
        sql.append(ColunmString.substring(0, ColunmString.length() - 2)).append(" )");
        Log.e(">>> ", sql.toString());
        return sql.toString();
    }
}
