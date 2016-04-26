package com.odoo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.odoo.auth.OdooAuthenticator;

import java.io.ByteArrayOutputStream;
import java.util.List;

import odoo.Odoo;
import odoo.helper.ODomain;
import odoo.helper.OUser;
import odoo.handler.OdooVersionException;
import odoo.helper.OdooFields;
import odoo.helper.utils.gson.OdooResult;
import odoo.listeners.IDatabaseListListener;
import odoo.listeners.IOdooConnectionListener;
import odoo.listeners.IOdooLoginCallback;
import odoo.listeners.IOdooResponse;
import odoo.listeners.OdooError;

public class LoginActivity extends AppCompatActivity implements IOdooLoginCallback, View.OnClickListener,
        IOdooConnectionListener {

    private EditText edtHost, edtUsername, edtPassword;
    private Button btnLogin;
    private Odoo odoo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /*
        TODO: Check for available user account, If found redirect to home screen.
         */

        //code for Login object Initialization
        edtHost = (EditText) findViewById(R.id.edtHost);
        edtUsername = (EditText) findViewById(R.id.edtUsername);
        edtPassword = (EditText) findViewById(R.id.edtPassword);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btnLogin) {

            edtHost.setError(null);
            if (edtHost.getText().toString().trim().isEmpty()) {
                edtHost.setError(getString(R.string.error_host_name_required));
                edtHost.requestFocus();
                return;
            }
            edtUsername.setError(null);
            if (edtUsername.getText().toString().trim().isEmpty()) {
                edtUsername.setError(getString(R.string.error_username_required));
                edtUsername.requestFocus();
                return;
            }
            edtPassword.setError(null);
            if (edtPassword.getText().toString().trim().isEmpty()) {
                edtPassword.setError(getString(R.string.error_password_required));
                edtPassword.requestFocus();
                return;
            }

            login();
        }
    }

    private void login() {
        String host_url = stripURL(edtHost.getText().toString().trim());
        try {
            odoo = Odoo.createInstance(this, host_url);
            odoo.setOnConnect(this);
        } catch (OdooVersionException e) {
            e.printStackTrace();
        }
    }

    private String stripURL(String host) {
        if (host.contains("http://") || host.contains("https://")) {
            return host;
        } else {
            return "http://" + host;
        }
    }


    @Override
    public void onConnect(final Odoo odoo) {
        odoo.getDatabaseList(new IDatabaseListListener() {
            @Override
            public void onDatabasesLoad(List<String> list) {
                if (list.size() > 1) {
                    // TODO: Show database selection dialog

                    String username = edtUsername.getText().toString().trim();
                    String password = edtPassword.getText().toString().trim();
                    String database = list.get(0);
                    odoo.authenticate(username, password, database, LoginActivity.this);

                } else {
                    // auto select first database and login.
                    String username = edtUsername.getText().toString().trim();
                    String password = edtPassword.getText().toString().trim();
                    String database = list.get(0);
                    odoo.authenticate(username, password, database, LoginActivity.this);
                }
            }
        });
    }

    @Override
    public void onError(OdooError odooError) {
        Log.e("odoo connection", odooError.getMessage(), odooError.getThrowable());
        Toast.makeText(this, "Unable to connect with odoo server", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginSuccess(Odoo odoo, OUser oUser) {

        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account account = new Account(oUser.getAndroidName(), OdooAuthenticator.AUTH_TYPE);
        if (manager.addAccountExplicitly(account, oUser.getPassword(), oUser.getAsBundle())) {
            //TODO: Redirect to home screen
            OdooFields fields=new OdooFields();
            fields.addAll(new String[]{"name","email"});
            ODomain domain = new ODomain();
            odoo.searchRead("res.partner", fields, domain, 0, 10, null, new IOdooResponse() {
                @Override
                public void onResponse(OdooResult odooResult) {
                    Log.v("RECORDS", odooResult + " ");
                }

                @Override
                public void onError(OdooError odooError) {

                }
            });


            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    @Override
    public void onLoginFail(OdooError odooError) {
        Toast.makeText(LoginActivity.this, R.string.invalid_username_or_password, Toast.LENGTH_SHORT).show();
    }

}
