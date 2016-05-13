package com.odoo;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.odoo.orm.ListRow;
import com.odoo.orm.OListAdapter;
import com.odoo.table.RecentContact;
import com.odoo.table.ResPartner;
import com.odoo.utils.BitmapUtils;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, OListAdapter.OnViewBindListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ResPartner resPartner;
    private OListAdapter oListAdapter;
    private ListView contactList;
    private RecentContact recentContact;

    private HashMap<Integer, Boolean> favToogleCache = new HashMap<>();

    public ContactFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        resPartner = new ResPartner(getContext());
        recentContact = new RecentContact(getContext());
        contactList = (ListView) view.findViewById(R.id.contactList);
        oListAdapter = new OListAdapter(getContext(), null, R.layout.contact_list_item);
        oListAdapter.setOnViewBindListener(this);
        contactList.setAdapter(oListAdapter);
        contactList.setOnItemClickListener(this);
        contactList.setOnItemLongClickListener(this);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, final ListRow row) {

        TextView textContactName, textContactEmail, textContactCity, textContactNumber;
        ImageView profileImage, isCompany;

        final ToggleButton toggleFavourite = (ToggleButton) view.findViewById(R.id.toggleIsFavourite);

        textContactName = (TextView) view.findViewById(R.id.textViewName);
        textContactEmail = (TextView) view.findViewById(R.id.textViewEmail);
        textContactCity = (TextView) view.findViewById(R.id.textViewCity);
        textContactNumber = (TextView) view.findViewById(R.id.textViewContact);
        profileImage = (ImageView) view.findViewById(R.id.profile_image);
        isCompany = (ImageView) view.findViewById(R.id.isCompany);

        String stringName, stringEmail, stringCity, stringMobile, stringImage, stringCompanyType,
                stringToggle;
        stringName = row.getString("name");
        stringEmail = row.getString("email");
        stringCity = row.getString("city");
        stringMobile = row.getString("mobile");
        stringImage = row.getString("image_medium");
        stringCompanyType = row.getString("company_type");
        stringToggle = row.getString("isFavourite");


        textContactName.setText(stringName);
        textContactEmail.setText(stringEmail);
        textContactEmail.setVisibility(stringEmail.equals("false") ? View.GONE : View.VISIBLE);

        textContactCity.setText(stringCity);
        textContactCity.setVisibility(stringCity.equals("false") ? View.GONE : View.VISIBLE);

        textContactNumber.setText(stringMobile);
        textContactNumber.setVisibility(stringMobile.equals("false") ? View.GONE : View.VISIBLE);

        isCompany.setVisibility(stringCompanyType.equals("person") ? View.GONE : View.VISIBLE);

        if (stringImage.equals("false")) {
            profileImage.setImageBitmap(BitmapUtils.getAlphabetImage(getContext(), stringName));
        } else {
            profileImage.setImageBitmap(BitmapUtils.getBitmapImage(getContext(),
                    stringImage));
        }

        boolean isFav = !stringToggle.equals("false");
        if (favToogleCache.containsKey(row.getInt("_id"))) {
            isFav = favToogleCache.get(row.getInt("_id"));
        } else {
            favToogleCache.put(row.getInt("_id"), isFav);
        }
        toggleFavourite.setChecked(isFav);
        toggleFavourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ContentValues values = new ContentValues();
                favToogleCache.put(row.getInt("_id"), toggleFavourite.isChecked());
                String favString = " marked favourite";
                if (toggleFavourite.isChecked()) {
                    values.put("isFavourite", "true");
                } else {
                    favString = " unmarked from favourite";
                    values.put("isFavourite", "false");
                }
                Toast.makeText(getContext(), "Contact " + favString, Toast.LENGTH_SHORT).show();
                resPartner.update(values, "_id = ? ", String.valueOf(row.getInt("_id")));
                getContext().getContentResolver().notifyChange(resPartner.uri(), null);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), resPartner.uri(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        oListAdapter.changeCursor(data);
        if (data.getCount() <= 0) {
            ((HomeActivity) getActivity()).syncData();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        oListAdapter.changeCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Cursor cr = (Cursor) oListAdapter.getItem(position);

        Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
        intent.putExtra("id", cr.getInt(cr.getColumnIndex("_id")));

        ContentValues values = new ContentValues();
        values.put("contact_id", cr.getInt(cr.getColumnIndex("_id")));
        recentContact.update_or_create(values, "contact_id = ? ", cr.getInt(cr.getColumnIndex("_id")) + "");
        getContext().getContentResolver().notifyChange(resPartner.uri(), null);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final Cursor cr = (Cursor) oListAdapter.getItem(position);
        final String stringMobileNumber = cr.getString(cr.getColumnIndex("mobile"));
        final String stringPhoneNumber = cr.getString(cr.getColumnIndex("phone"));
        final String stringEmail = cr.getString(cr.getColumnIndex("email"));

        final CharSequence[] options = {"Delete", "Call", "Send Mail"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); //
        builder.setTitle("SELECT ANY ONE!");
        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int item) {

                arg0.dismiss();
                if (options[item].equals("Delete")) {
                    resPartner.delete("_id = ? ", String.valueOf(cr.getInt(cr.getColumnIndex("_id"))));
                    Toast.makeText(getContext(), "Contact Delete", Toast.LENGTH_SHORT).show();
                }
                if (options[item].equals("Call")) {
                    if (stringMobileNumber.equals("false")) {
                        if (stringPhoneNumber.equals("false")) {
                            Toast.makeText(getContext(), "Number not found", Toast.LENGTH_LONG).show();
                        } else {
                            callToContact(stringPhoneNumber);
                        }
                    } else {
                        callToContact(stringMobileNumber);
                    }
                }

                if (options[item].equals("Send Mail")) {
                    if (stringEmail.equals("false")) {
                        Toast.makeText(getContext(), "Email not found", Toast.LENGTH_LONG).show();
                    } else {
                        Intent mailIntent = new Intent(Intent.ACTION_SEND);
                        mailIntent.setData(Uri.parse("mailto:"));
                        mailIntent.setType("text/plain");
                        mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{stringEmail});
                        startActivity(mailIntent);
                    }
                }
            }
        });
        builder.show();
        return true;
    }

    public void callToContact(String number) {
        Uri phoneCall;
        Intent dial;
        phoneCall = Uri.parse("tel:" + number);
        dial = new Intent(Intent.ACTION_CALL, phoneCall);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Call Permission required", Toast.LENGTH_SHORT).show();
        } else {
            startActivity(dial);
        }
    }
}
