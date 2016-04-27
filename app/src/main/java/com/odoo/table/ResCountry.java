package com.odoo.table;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.ColumnType;

/**
 * Created by dpr on 27/4/16.
 */
public class ResCountry extends OModel {

    OColumn name = new OColumn("Name", ColumnType.VARCHAR);

    public ResCountry(Context context) {
        super(context, "res.country");
    }
}
