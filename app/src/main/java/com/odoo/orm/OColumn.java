package com.odoo.orm;

import com.odoo.orm.types.ColumnType;

public class OColumn {
    public String name, label, relModel;
    public ColumnType columnType;
    public Boolean primaryKey = false, autoIncrement = false;
    public Object defValue = null;

    public OColumn(String label, ColumnType columnType) {
        this(label, columnType, null);
    }

    public OColumn(String label, ColumnType columnType, String relModel) {
        this.label = label;
        this.columnType = columnType;
        this.relModel = relModel;
    }

    public OColumn makePrimaryKey() {
        primaryKey = true;
        return this;
    }

    public OColumn makeAutoIncrement() {
        autoIncrement = true;
        return this;
    }

    public OColumn setDefault(Object defValue) {
        this.defValue = defValue;
        return this;
    }
}
