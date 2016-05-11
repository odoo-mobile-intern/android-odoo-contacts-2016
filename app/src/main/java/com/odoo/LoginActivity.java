package com.odoo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.odoo.auth.OdooAuthenticator;
import com.odoo.orm.sync.ContactSyncAdapter;

import java.util.List;

import odoo.Odoo;
import odoo.handler.OdooVersionException;
import odoo.helper.OUser;
import odoo.listeners.IDatabaseListListener;
import odoo.listeners.IOdooConnectionListener;
import odoo.listeners.IOdooLoginCallback;
import odoo.listeners.OdooError;

public class LoginActivity extends AppCompatActivity implements IOdooLoginCallback, View.OnClickListener,
        IOdooConnectionListener {

    private EditText edtHost, edtUsername, edtPassword;
    private Button btnLogin;
    private Odoo odoo;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = manager.getAccountsByType(OdooAuthenticator.AUTH_TYPE);
        if (accounts.length > 0) {
            // account found. redirecting to home screen.
            redirectToHome();
        }

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
                    showDatabaseSelection(list);
                } else {
                    // auto select first database and login.
                    loginTo(list.get(0));
                }
            }
        });
    }

    private void loginTo(String database) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        odoo.authenticate(username, password, database, this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setMessage(getString(R.string.login_in_progress));
        progressDialog.show();
    }

    @Override
    public void onError(OdooError odooError) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        Log.e("odoo connection", odooError.getMessage(), odooError.getThrowable());
        Toast.makeText(this, R.string.unable_to_connect_odoo, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginSuccess(Odoo odoo, OUser oUser) {
        progressDialog.dismiss();
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account account = new Account(oUser.getAndroidName(), OdooAuthenticator.AUTH_TYPE);
        if (manager.addAccountExplicitly(account, oUser.getPassword(), oUser.getAsBundle())) {
            ContentResolver.setSyncAutomatically(account, ContactSyncAdapter.AUTHORITY, true);
            redirectToHome();
        }
    }

    @Override
    public void onLoginFail(OdooError odooError) {
        progressDialog.dismiss();
        Toast.makeText(LoginActivity.this, R.string.invalid_username_or_password, Toast.LENGTH_SHORT).show();
    }

    private void redirectToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }


    private void showDatabaseSelection(final List<String> databases) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_database);
        builder.setSingleChoiceItems(databases.toArray(new String[databases.size()]), 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String database = databases.get(which);
                        loginTo(database);
                    }
                });
        builder.create().show();
    }


}
