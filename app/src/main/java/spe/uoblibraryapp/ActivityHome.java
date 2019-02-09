package spe.uoblibraryapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import spe.uoblibraryapp.api.AuthService;
import spe.uoblibraryapp.api.ncip.WMSNCIPService;
import stanford.androidlib.SimpleActivity;

public class ActivityHome extends SimpleActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Home-Activity";
    private FragmentCustomPagerAdapter mAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = $(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = $(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = $(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_current_loans_reservations);
        navigationView.setNavigationItemSelectedListener(this);

        mViewPager = $(R.id.container);

        //This is the new page change listener to fix action bar title not changing on horizontally swiping through pages
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mViewPager.setCurrentItem(position);
                navigationView.getMenu().getItem(position).setChecked(true);
                switch (position) {
                    case 0:
                        getSupportActionBar().setTitle("Loans");
                        break;
                    case 1:
                        getSupportActionBar().setTitle("Reservations");
                        break;
                    case 2:
                        getSupportActionBar().setTitle("App Settings");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        setupViewPager(mViewPager);
        mViewPager.setCurrentItem(0);
        setTitle(mAdapter.getFragmentTitle(0));


        // Floating action button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(ActivityHome.this, "Scan New Book", Toast.LENGTH_SHORT);
                toast.show();
                Intent NFCActivity = new Intent(ActivityHome.this, ActivityScanNFC.class);
                startActivity(NFCActivity);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = $(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //TODO: Exit prompt.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /*Action bar menu options*/
        if (id == R.id.action_logout) {
            //Action bar logout
            AuthService.enqueueWork(this, AuthService.class, AuthService.jobId, new Intent(Constants.IntentActions.AUTH_LOGOUT));
            return true;
        }else if (id == R.id.action_refresh) {
            Intent getUserProfileIntent = new Intent(Constants.IntentActions.LOOKUP_USER);
            WMSNCIPService.enqueueWork(this, WMSNCIPService.class, WMSNCIPService.jobId, getUserProfileIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_current_loans_reservations) {
            setViewPager("Loans");
        } else if (id == R.id.nav_reservations) {
            setViewPager("Reservation");
        } else if (id == R.id.nav_settings) {
            setViewPager("Settings");
        }

        DrawerLayout drawer = $(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupViewPager(ViewPager viewPager) {
        mAdapter = new FragmentCustomPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new FragmentLoans(), "Loans");
        mAdapter.addFragment(new FragmentReservation(), "Reservation");
        mAdapter.addFragment(new FragmentSettings(), "Settings");
        viewPager.setAdapter(mAdapter);
    }

    public void setViewPager(String fragmentName) {
        int index = mAdapter.getFragmentIndex(fragmentName);

        //Show Appropriate Title
        setTitle(fragmentName);
        mViewPager.setCurrentItem(index);
    }
}
