package com.odoo.table;

import android.content.Context;

import com.odoo.database.ContactDatabase;

import java.util.HashMap;

/**
 * Created by sha on 25/4/16.
 */
public class AllTables {
    public HashMap<String, ContactDatabase> tables (Context context){
        HashMap<String, ContactDatabase> tableObjects= new HashMap<>();
        tableObjects.put("ContactTable",new ContactTable(context));
        return tableObjects;
    }
}
