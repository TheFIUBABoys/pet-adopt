package com.fiuba.tdp.petadopt.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.fiuba.tdp.petadopt.R;
import com.fiuba.tdp.petadopt.fragments.MatchesFragment;
import com.fiuba.tdp.petadopt.fragments.MyPetsFragment;
import com.fiuba.tdp.petadopt.fragments.SearchFragment;
import com.fiuba.tdp.petadopt.fragments.SettingsFragment;
import com.fiuba.tdp.petadopt.model.User;
import com.fiuba.tdp.petadopt.service.UserPersistenceService;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;

import com.fiuba.tdp.petadopt.service.PetsClient;

public class MainActivity extends AppCompatActivity {
    private String[] optionTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private PetsClient client;
    private int initialFragmentIndex = 0;
    private String auth_token;
    private Boolean created = false;
    private Boolean exit = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserPersistenceService ups = new UserPersistenceService(this);
        User user = ups.getUserIfPresent();
        if (user == null) {
            promptLogin();
        } else {
            auth_token = user.getAuthToken();
            setupActivity();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        UserPersistenceService ups = new UserPersistenceService(this);
        User user = ups.getUserIfPresent();
        if (user == null) {
            finish();
        } else {
            auth_token = user.getAuthToken();
            setupActivity();
        }

    }

    private void setupActivity() {
        if (!created) {
            DrawerItemClickListener listener = new DrawerItemClickListener();

            setContentView(R.layout.activity_main);

            optionTitles = getResources().getStringArray(R.array.drawer_option_array);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerList = (ListView) findViewById(R.id.left_drawer);

            // Set the adapter for the list view
            mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, optionTitles));

            // Set the list's click listener
            mDrawerList.setOnItemClickListener(listener);
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    invalidateOptionsMenu();
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    invalidateOptionsMenu();
                }
            };
            listener.displayView(initialFragmentIndex);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }

            this.fetchPets();
            created = true;
        }
    }

    private void promptLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (created) {
            mDrawerToggle.syncState();
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, int position,long id) {

            // String text= "menu click... should be implemented";
            // Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
            displayView(position);
        }

        private void displayView(int position) {
            // update the main content by replacing fragments
            // TODO: find way to avoid switch
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new MatchesFragment();
                    break;
                case 1:
                    fragment = new SearchFragment();
                    break;
                case 2:
                    fragment = new MyPetsFragment();
                    break;
                case 3:
                    fragment = new SettingsFragment();
                    break;
                case 4:
                    goBackToLogin();
                    break;
                default:
                    break;
            }

            if (fragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment).commit();

                // Highlight the selected item, update the title, and close the drawer
                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                setTitle(optionTitles[position]);
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                // error in creating fragment
                Log.e("MainActivity", "Error in creating fragment");
            }
        }
    }

    private void fetchPets() {
        client = new PetsClient(auth_token);
        client.getPets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int code, Header[] headers, JSONArray body) {
                String items = "";
                try {
                    items = body.toString();
                    Log.v("JSON", items);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void goBackToLogin() {
        UserPersistenceService ups = new UserPersistenceService(this);
        ups.destroyUserData();
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity

        } else {
            Toast.makeText(this, R.string.exit_toast,
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
    }
}