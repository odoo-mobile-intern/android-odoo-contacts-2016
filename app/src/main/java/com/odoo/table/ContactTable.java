package com.odoo.table;

import android.content.Context;

import com.odoo.database.ColumnType;
import com.odoo.database.ContactColumn;
import com.odoo.database.ContactDatabase;

/**
 * Created by sha on 25/4/16.
 */
public class ContactTable extends ContactDatabase {
    public ContactTable(Context context) {
        super(context,"res.partner");

        AddColumns(new ContactColumn("id", ColumnType.INTEGER));
        AddColumns(new ContactColumn("name", ColumnType.VARCHAR));
        AddColumns(new ContactColumn("street", ColumnType.VARCHAR));
        AddColumns(new ContactColumn("city", ColumnType.VARCHAR));
        AddColumns(new ContactColumn("image", ColumnType.BLOB));
        AddColumns(new ContactColumn("phone", ColumnType.VARCHAR));
        AddColumns(new ContactColumn("mobile", ColumnType.VARCHAR));
        AddColumns(new ContactColumn("function", ColumnType.VARCHAR));// job position
        AddColumns(new ContactColumn("email", ColumnType.VARCHAR));
    }
}
