package com.odoo;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.odoo.orm.ListRow;
import com.odoo.orm.OListAdapter;
import com.odoo.utils.BitmapUtils;

public class SearchContactActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, TextWatcher, OListAdapter.OnViewBindListener, AdapterView.OnItemClickListener {

    private OListAdapter adapter;
    private ListView listView;
    private EditText edtSearchBox;
    private String searchFor = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_contact_activity);
        edtSearchBox = (EditText) findViewById(R.id.edtSearchBox);
        edtSearchBox.addTextChangedListener(this);
        init();
    }

    private void init() {
        listView = (ListView) findViewById(R.id.contactList);
        adapter = new OListAdapter(this, null, R.layout.contact_list_item);
        adapter.setOnViewBindListener(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        Uri uri = Uri.parse("content://com.odoo.contacts.res_partner/res_partner");

        String where = null;
        String[] args = null;
        if (searchFor != null) {
            where = " name like ?";
            args = new String[]{"%" + searchFor + "%"};
        }
        return new CursorLoader(this, uri, null, where, args, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ListRow row) {

        TextView textContactName, textContactEmail, textContactCity, textContactNumber;
        ImageView profileImage, isCompany;

        final ToggleButton toggleFavourite = (ToggleButton) view.findViewById(R.id.toggleIsFavourite);

        textContactName = (TextView) view.findViewById(R.id.textViewName);
        textContactEmail = (TextView) view.findViewById(R.id.textViewEmail);
        textContactCity = (TextView) view.findViewById(R.id.textViewCity);
        textContactNumber = (TextView) view.findViewById(R.id.textViewContact);
        profileImage = (ImageView) view.findViewById(R.id.profile_image);
        isCompany = (ImageView) view.findViewById(R.id.isCompany);

        String stringName, stringEmail, stringCity, stringMobile, stringImage, stringCompanyType;
        stringName = row.getString("name");
        stringEmail = row.getString("email");
        stringCity = row.getString("city");
        stringMobile = row.getString("mobile");
        stringImage = row.getString("image_medium");
        stringCompanyType = row.getString("company_type");


        textContactName.setText(stringName);
        textContactEmail.setText(stringEmail);
        textContactEmail.setVisibility(stringEmail.equals("false") ? View.GONE : View.VISIBLE);

        textContactCity.setText(stringCity);
        textContactCity.setVisibility(stringCity.equals("false") ? View.GONE : View.VISIBLE);

        textContactNumber.setText(stringMobile);
        textContactNumber.setVisibility(stringMobile.equals("false") ? View.GONE : View.VISIBLE);

        isCompany.setVisibility(stringCompanyType.equals("person") ? View.GONE : View.VISIBLE);

        if (stringImage.equals("false")) {
            profileImage.setImageBitmap(BitmapUtils.getAlphabetImage(this, stringName));
        } else {
            profileImage.setImageBitmap(BitmapUtils.getBitmapImage(this,
                    stringImage));
        }
        toggleFavourite.setVisibility(View.GONE);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        searchFor = s.toString();
        if (s.toString().trim().isEmpty()) {
            searchFor = null;
        }
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cr = (Cursor) adapter.getItem(position);
        Intent intent = new Intent(this, ContactDetailActivity.class);
        intent.putExtra("id", cr.getInt(cr.getColumnIndex("_id")));
        startActivity(intent);
        finish();
    }
}
