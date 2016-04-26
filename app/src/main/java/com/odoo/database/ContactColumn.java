package com.odoo.database;

/**
 * Created by sha on 25/4/16.
 */
public class ContactColumn {
    public String FieldName;
    public ColumnType columnType;
    public Boolean primaryKey=false, autoIncreament=false;

    public ContactColumn(String fieldName, ColumnType columnType) {
        this.FieldName = fieldName;
        this.columnType = columnType;
    }

    public ContactColumn makePrimaryKey() {
        primaryKey=true;
        return this;
    }

    public  ContactColumn makeautoIncreament(){
        autoIncreament=true;
        return this;
    }
}
