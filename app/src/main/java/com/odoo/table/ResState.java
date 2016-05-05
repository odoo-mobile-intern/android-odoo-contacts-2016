package com.odoo.table;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.ColumnType;

public class ResState extends OModel {

    OColumn name = new OColumn("Name", ColumnType.VARCHAR);

    public ResState(Context context) {
        super(context, "res.state");
    }
}
