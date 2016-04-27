package com.odoo.orm;

import android.util.Log;

/**
 * Created by sha on 25/4/16.
 */
public class StatementBuilder {
    private OModel mTable;

    public StatementBuilder(OModel table) {
        this.mTable = table;
    }

    public String createStatement() {

        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE IF NOT EXISTS ")
                .append(mTable.getTableName())
                .append(" (");

        StringBuffer columns = new StringBuffer();
        for (OColumn column : mTable.getColumn()) {
            columns.append(column.name)
                    .append(" ")
                    .append(column.columnType.toString());

            if (column.primaryKey) {
                columns.append(" PRIMARY KEY ");
            }
            if (column.autoIncrement) {
                columns.append(" AUTOINCREMENT ");
            }
            if (column.defValue != null) {
                columns.append(" DEFAULT '").append(column.defValue.toString()).append("'");
            }
            columns.append(" , ");
        }
        String columnString = columns.toString();
        sql.append(columnString.substring(0, columnString.length() - 2)).append(" )");
        Log.e(">>> ", sql.toString());
        return sql.toString();
    }
}
