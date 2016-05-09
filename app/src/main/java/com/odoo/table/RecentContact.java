package com.odoo.table;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.ColumnType;

/**
 * Created by sha on 9/5/16.
 */
public class RecentContact extends OModel {

    OColumn contact_id = new OColumn("Contact Id", ColumnType.INTEGER);
    OColumn write_date = new OColumn("Write Date", ColumnType.VARCHAR);

    public RecentContact(Context context) {
        super(context, "RecentContact");
    }
}
