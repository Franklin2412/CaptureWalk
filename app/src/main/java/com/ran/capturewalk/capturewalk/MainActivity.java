package com.ran.capturewalk.capturewalk;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        ((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(this);
        showFragment(new WalkMapFragment());
    }

    public void showFragment(Fragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle((CharSequence) title);
    }

    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen((int) MediaRouterJellybean.ALL_ROUTE_TYPES)) {
            drawer.closeDrawer((int) MediaRouterJellybean.ALL_ROUTE_TYPES);
        } else {
            super.onBackPressed();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_capture_your_walk) {
            setToolbarTitle(getString(R.string.navigation_capture_your_walk));
            showFragment(new WalkMapFragment());
        } else if (id == R.id.nav_view_history) {
            setToolbarTitle(getString(R.string.navigation_view_history));
            showFragment(new ViewHistoryFragment());
        } else if (id == R.id.nav_profile) {
            setToolbarTitle(getString(R.string.navigation_profile));
            showFragment(new ProfileFragment());
        } else if (id == R.id.nav_settings) {
            setToolbarTitle(getString(R.string.navigation_settings));
            showFragment(new SettingsFragment());
        } else if (id == R.id.nav_about_us) {
            setToolbarTitle(getString(R.string.navigation_about_us));
            showFragment(new AboutUsFragment());
        }
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer((int) MediaRouterJellybean.ALL_ROUTE_TYPES);
        return true;
    }
}
