package com.odoo.orm.types;

/**
 * Created by sha on 25/4/16.
 */
public enum ColumnType {
    VARCHAR("VARCHAR"),
    INTEGER("INTEGER"),
    BLOB("BLOB"),
    MANY2ONE("INTEGER"),
    DATETIME("varchar"),
    BOOLEAN("boolean");

    String type;

    ColumnType(String type) {
        this.type = type;
    }
}
