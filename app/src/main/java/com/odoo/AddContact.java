package com.odoo;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.odoo.table.ResPartner;
import com.odoo.utils.BitmapUtils;

public class AddContact extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private EditText editName, editMobileNumber, editPhoneNumber, editCity, editEmail, editState, editCountry,
            editPincode, editWebsite, editFax, editStreet, editStreet2;
    private String imageString;
    private ResPartner resPartner;
    private ImageView profileImage;
    private CheckBox checkBoxIsCompany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        init();
    }

    private void init() {

        resPartner = new ResPartner(this);

        profileImage = (ImageView) findViewById(R.id.avatar);
        profileImage.setOnClickListener(this);

        editName = (EditText) findViewById(R.id.editName);
        editMobileNumber = (EditText) findViewById(R.id.editMobileNumber);
        editPhoneNumber = (EditText) findViewById(R.id.editPhoneNumber);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editStreet = (EditText) findViewById(R.id.editStreet);
        editStreet2 = (EditText) findViewById(R.id.editStreet2);
        editCity = (EditText) findViewById(R.id.editCity);
        editState = (EditText) findViewById(R.id.editState);
        editCountry = (EditText) findViewById(R.id.editCountry);
        editPincode = (EditText) findViewById(R.id.editPincode);
        editWebsite = (EditText) findViewById(R.id.editWebsite);
        editFax = (EditText) findViewById(R.id.editFax);
        checkBoxIsCompany = (CheckBox) findViewById(R.id.checkboxIsCompany);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_contact_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menuSave) {

            editName.setError(null);
            if (TextUtils.isEmpty(editName.getText())) {
                editName.setError("Name Required");
                editName.requestFocus();
                return true;
            }

            ContentValues values = new ContentValues();

            values.put("name", editName.getText().toString());
            values.put("image_medium", imageString);

            if (checkBoxIsCompany.isChecked()) {
                values.put("company_type", "company");
            } else {
                values.put("company_type", "person");
            }

            if (editMobileNumber.getText().toString().equals("")) {
                values.put("mobile", "false");
            } else {
                values.put("mobile", editMobileNumber.getText().toString());
            }

            if (editPhoneNumber.getText().toString().equals("")) {
                values.put("phone", "false");
            } else {
                values.put("phone", editPhoneNumber.getText().toString());
            }

            if (editCity.getText().toString().equals("")) {
                values.put("city", "false");
            } else {
                values.put("city", editCity.getText().toString());
            }

            if (editStreet.getText().toString().equals("")) {
                values.put("street", "false");
            } else {
                values.put("street", editStreet.getText().toString());
            }

            if (editStreet2.getText().toString().equals("")) {
                values.put("street2", "false");
            } else {
                values.put("street2", editStreet2.getText().toString());
            }

            if (editEmail.getText().toString().equals("")) {
                values.put("email", "false");
            } else {
                values.put("email", editEmail.getText().toString());
            }

            if (editWebsite.getText().toString().equals("")) {
                values.put("website", "false");
            } else {
                values.put("website", editWebsite.getText().toString());
            }

            if (editState.getText().toString().equals("")) {
                values.put("state_id", "0");
            } else {
                //TODO: state name

                values.put("state_id", "1");
            }

            if (editCountry.getText().toString().equals("")) {
                values.put("country_id", "0");
            } else {
                //TODO: Country name
                values.put("country_id", "1");
            }

            if (editFax.getText().toString().equals("")) {
                values.put("fax", "false");
            } else {
                values.put("fax", editFax.getText().toString());
            }

            if (editPincode.getText().toString().equals("")) {
                values.put("zip", "false");
            } else {
                values.put("zip", editPincode.getText().toString());
            }

            resPartner.create(values);
            Toast.makeText(this, R.string.new_contact_create, Toast.LENGTH_SHORT).show();
            finish();
        }

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        selectImage();
    }

    private void selectImage() {
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);

        try {
            intent.putExtra("return-data", true);
            startActivityForResult(Intent.createChooser(intent,
                    "Complete action using"), 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {

            if (data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap bitmap = extras.getParcelable("data");
                    profileImage.setImageBitmap(bitmap);
                    imageString = BitmapUtils.bitmapToBase64(bitmap);
                }
            } else {
                NavUtils.getParentActivityIntent(this);
            }
        }
    }
}