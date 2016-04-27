package com.odoo.table;

import android.content.Context;

import com.odoo.orm.OModel;

import java.util.HashMap;

public class ModelRegistry {
    public HashMap<String, OModel> models(Context context) {
        HashMap<String, OModel> models = new HashMap<>();
        models.put("res.partner", new ResPartner(context));
        models.put("res.country", new ResCountry(context));
        models.put("res.state", new ResState(context));
        return models;
    }
}
