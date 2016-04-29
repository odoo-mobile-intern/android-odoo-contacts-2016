package com.odoo.table;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.ColumnType;

public class ResCountry extends OModel {

    OColumn name = new OColumn("Name", ColumnType.VARCHAR);

    public ResCountry(Context context) {
        super(context, "res.country");
    }
}
