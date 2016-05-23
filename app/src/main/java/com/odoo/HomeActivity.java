package com.odoo;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.odoo.auth.OdooAuthenticator;
import com.odoo.orm.sync.ContactSyncAdapter;
import com.odoo.table.RecentContact;
import com.odoo.table.ResPartner;

public class HomeActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final int REQUEST_CODE_ASK_PERMISSIONS_READ_CONTACTS = 11;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private SearchView searchview;
    private ResPartner resPartner;
    private RecentContact recentContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        resPartner = new ResPartner(this);
        recentContact = new RecentContact(this);

        searchview = (SearchView) findViewById(R.id.contactSearchView);
        searchview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirecting to global contact search activity
                startActivity(new Intent(HomeActivity.this, SearchContactActivity.class));
            }
        });


        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        //code for tab
        tabLayout.addTab(tabLayout.newTab().setText("Recent"));
        tabLayout.addTab(tabLayout.newTab().setText("Contacts"));
        tabLayout.addTab(tabLayout.newTab().setText("Favourite"));
        tabLayout.setOnTabSelectedListener(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //code for swipe
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, AddContact.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    public void syncData() {
        AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByType(OdooAuthenticator.AUTH_TYPE);
        if (accounts.length == 1) {
            ContentResolver.requestSync(accounts[0], ContactSyncAdapter.AUTHORITY,
                    Bundle.EMPTY);
            Toast.makeText(HomeActivity.this, "Sync started", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_sync:
                syncData();
                break;
            case R.id.menu_remove_fav_contact:
                ContentValues values = new ContentValues();
                values.put("isFavourite", "false");
                resPartner.update(values, "isFavourite = ? ", new String[]{"true"});
                this.getContentResolver().notifyChange(resPartner.uri(), null);
                break;
            case R.id.menu_remove_recent_contact:
                int count = resPartner.count();
                if (count > 0) {
                    recentContact.delete(null);
                    this.getContentResolver().notifyChange(resPartner.uri(), null);

                } else
                    Toast.makeText(HomeActivity.this, "No any contact to remove", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_import_contact:

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_ASK_PERMISSIONS_READ_CONTACTS);
                    }
                } else importContacts();

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void importContacts() {
        ContentResolver cr = this.getContentResolver();

        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int contact_id = cursor.getInt(cursor
                            .getColumnIndex(BaseColumns._ID));
                    String contact_name = cursor.getString(cursor
                            .getColumnIndex("display_name"));
                    String contact_image = cursor.getString(cursor
                            .getColumnIndex("photo_uri"));

                    Cursor phoneCR = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                    + " = ?", new String[]{contact_id + ""},
                            null);

                    phoneCR.moveToFirst();
                    String contact_number = phoneCR
                            .getString(phoneCR
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));


                    ContentValues values = new ContentValues();
                    values.put("name", contact_name);
                    values.put("image_medium", contact_image);
                    values.put("mobile", contact_number);
                    resPartner.update_or_create(values, "name = ? ", new String[]{contact_name});


                } while (cursor.moveToNext());
                Log.d("TAG", cursor.getCount() + " contacts import");
                cursor.close();
            }
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_recent, container, false);
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    RecentFragment recentFragment = new RecentFragment();
                    return recentFragment;
                case 1:
                    ContactFragment contactFragment = new ContactFragment();
                    return contactFragment;
                case 2:
                    FavouriteFragment favouriteFragment = new FavouriteFragment();
                    return favouriteFragment;
            }
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {

                case 0:
                    return "Recent";
                case 1:
                    return "Contacts";
                case 2:
                    return "Favourite";

            }
            return null;
        }
    }

}
