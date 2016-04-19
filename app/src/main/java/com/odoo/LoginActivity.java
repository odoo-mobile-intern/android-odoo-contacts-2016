package com.odoo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.odoo.auth.OdooAuthenticator;

import odoo.Odoo;
import odoo.helper.OUser;
import odoo.handler.OdooVersionException;
import odoo.listeners.IOdooConnectionListener;
import odoo.listeners.IOdooLoginCallback;
import odoo.listeners.OdooError;

public class LoginActivity extends AppCompatActivity implements IOdooLoginCallback, View.OnClickListener {

    private EditText host,user,pwd;
    private Button button;
    public Odoo odoo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //code for Login object Initialization
        host = (EditText) findViewById(R.id.host);
        user = (EditText) findViewById(R.id.user);
        pwd = (EditText) findViewById(R.id.password);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);


        //code for create account
        AccountManager manager=(AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account newAccount= new Account("meghavyas-odoo", OdooAuthenticator.AUTH_TYPE);
        Bundle userdata=new Bundle();
        userdata.putString("host1","http://jfjnnjf");
        userdata.putString("db", "imvmv");

        manager.addAccountExplicitly(newAccount,"test",userdata);

        Account[] accounts=manager.getAccountsByType(OdooAuthenticator.AUTH_TYPE);

        Log.e(">>> COUNT", accounts.length + "<<<");

        for (Account account:accounts){
            Log.e("acc",account.name+" : "+account.type);
            String host1=manager.getUserData(account,"host1");
        }


    }

    @Override
    public void onClick(View v) {

        Log.e(">>>>>>","http://" + host.getText().toString());
        Log.e(">>>>>>",user.getText().toString());
        Log.e(">>>>>>",pwd.getText().toString());

        try {
            odoo= Odoo.createInstance(this,"http://" +  host.getText().toString());
            odoo.setOnConnect(new IOdooConnectionListener() {
                @Override
                public void onConnect(Odoo odoo) {
                    odoo.authenticate(user.getText().toString(),pwd.getText().toString(),"142733-9-0-db0def-all",LoginActivity.this);


                }

                @Override
                public void onError(OdooError odooError) {

                }
            });



        }catch (OdooVersionException e){
            e.printStackTrace();
        }


    }

    @Override
    public void onLoginSuccess(Odoo odoo, OUser oUser) {
        Toast.makeText(this,"Successfull login", Toast.LENGTH_SHORT).show();
        Log.e(">>>Success",oUser.getName());

    }

    @Override
    public void onLoginFail(OdooError odooError) {
        Toast.makeText(this,"Not sucessfull login", Toast.LENGTH_SHORT).show();
        Log.e(">>>fail",odooError+"");

    }
}
