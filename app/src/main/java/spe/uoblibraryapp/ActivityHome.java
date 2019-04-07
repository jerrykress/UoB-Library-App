package spe.uoblibraryapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import spe.uoblibraryapp.api.AuthService;
import spe.uoblibraryapp.api.IMService;
import spe.uoblibraryapp.api.ncip.WMSNCIPService;

public class ActivityHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Home-Activity";
    private FragmentCustomPagerAdapter mAdapter;
    private ViewPager mViewPager; //container holding the fragments.
    private MyBroadCastReceiver myBroadCastReceiver;
    private Boolean phoneHasNFC = false;
    private SharedPreferences userPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter!=null){
            phoneHasNFC = true;
        }
        myBroadCastReceiver = new MyBroadCastReceiver();

        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_dash);
        navigationView.setNavigationItemSelectedListener(this);

        mViewPager = findViewById(R.id.container);

        //This is the new page change listener to fix action bar title not changing on horizontally swiping through pages
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                mViewPager.setCurrentItem(position);
                //Pos+1 because this position is from the fragment manager. Fragment 0 corresponds to Navigation View 1.
                switch (position) {
                    case 0:
                        getSupportActionBar().setTitle("Dashboard");
                        break;
                    case 1:
                        getSupportActionBar().setTitle("Loans");
                        break;
                    case 2:
                        getSupportActionBar().setTitle("Reservations");
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //Load all the fragments & Select Index 0 [Dashboard].
        createViewPager(mViewPager);
        mViewPager.setCurrentItem(0);
        setTitle(mAdapter.getFragmentTitle(0));

        // Adds name and email to homepage
        userPrefs = getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        View v = navigationView.getHeaderView(0);
        TextView userName = v.findViewById(R.id.user_name);
        TextView userEmail = v.findViewById(R.id.user_email);

        userEmail.setText(userPrefs.getString("email", ""));
        userName.setText(userPrefs.getString("name", ""));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Scan a New Book.
                if (phoneHasNFC && isOnline() && (!isOverdraw())) {
                    Intent LibraryNFCActivity = new Intent(ActivityHome.this, ActivityLibrarySelect.class);
                    startActivity(LibraryNFCActivity);
                }
                else if(!isOnline()){
                    Toast.makeText(getApplicationContext(), "Could not connect to server. Please check your network settings.", Toast.LENGTH_LONG).show();
                }
                else if(isOverdraw()){
                    Toast.makeText(getApplicationContext(), "You have reached your loan limit.", Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "Feature Disabled, your phone does not support NFC.", Toast.LENGTH_LONG).show();
            }
        });

        // Get user account details.
        IMService.enqueueWork(getApplicationContext(), IMService.class, IMService.jobId, new Intent(Constants.IntentActions.LOOKUP_USER_ACCOUNT));

    }

    public boolean isOverdraw(){
        CacheManager cacheManager = CacheManager.getInstance();
        SharedPreferences prefs = getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        if(cacheManager.getUserProfile().getLoans().size() >= Constants.LibraryDetails.borrowerCategories.get(prefs.getString("borrowerCategory", "")))
            return true;
        else
            return false;
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 132.145.54.223");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getViewPager()==0){
                //exit app
                super.onBackPressed();
            }
            else setViewPager("Dashboard");
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
        }
        else if (id == R.id.action_refresh) {
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

        if (id == R.id.nav_scan) {
            boolean accountBlocked = userPrefs.getBoolean("accountBlocked", true);
            if (phoneHasNFC && !accountBlocked && isOnline() && (!isOverdraw())) {
                startActivity(new Intent(this, ActivityLibrarySelect.class));
            } else if (accountBlocked) {
                Toast.makeText(getApplicationContext(), "Feature Disabled, your account is blocked.", Toast.LENGTH_LONG).show();
                return false;
            } else if(!isOnline()){
                Toast.makeText(getApplicationContext(), "Could not connect to server. Please check your network settings.", Toast.LENGTH_LONG).show();
                return false;
            } else if(isOverdraw()){
                Toast.makeText(getApplicationContext(), "You have reached your loan limit.", Toast.LENGTH_LONG).show();
                return false;
            } else{
                Toast.makeText(getApplicationContext(), "Feature Disabled, your phone does not support NFC.", Toast.LENGTH_LONG).show();
                return false;
            }


        } else if (id == R.id.nav_dash) {
            setViewPager("Dashboard");
        } else if (id == R.id.nav_current_loans_reservations) {
            setViewPager("Loans");
        } else if (id == R.id.nav_reservations) {
            setViewPager("Reservation");
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createViewPager(ViewPager viewPager) {
        mAdapter = new FragmentCustomPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new FragmentDashboard(), "Dashboard");
        mAdapter.addFragment(new FragmentLoans(), "Loans");
        mAdapter.addFragment(new FragmentReservation(), "Reservation");
        viewPager.setAdapter(mAdapter);
    }

    public void setViewPager(String fragmentName) {
        if (mAdapter.fragmentExists(fragmentName)){
            //Fragment exists, change title & view.
            setTitle(fragmentName);
            mViewPager.setCurrentItem(mAdapter.getFragmentIndex(fragmentName));
        }
        else {
            //Default to First Fragment...
            setTitle(mAdapter.getFragmentTitle(0));
            mViewPager.setCurrentItem(0);
        }
    }

    public int getViewPager() {
        return mViewPager.getCurrentItem();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.IntentActions.LOOKUP_USER_ACCOUNT_RESPONSE);
            intentFilter.addAction(Constants.IntentActions.LOOKUP_USER_ACCOUNT_ERROR);
            registerReceiver(myBroadCastReceiver, intentFilter);
            Log.d(TAG, "Receiver Registered");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(myBroadCastReceiver);
        Log.d(TAG, "Receiver Unregistered");
        super.onPause();
    }

    class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d(TAG, "onReceive() called");
                if (Constants.IntentActions.LOOKUP_USER_ACCOUNT_RESPONSE.equals(intent.getAction())) {
                    SharedPreferences prefs = getSharedPreferences("userDetails", Context.MODE_PRIVATE);
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    View v = navigationView.getHeaderView(0);
                    TextView userName = v.findViewById(R.id.user_name);
                    TextView userEmail = v.findViewById(R.id.user_email);

                    userEmail.setText(prefs.getString("email", ""));
                    userName.setText(prefs.getString("name", ""));
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot lookup user profile", Toast.LENGTH_LONG).show();
//                    Log.e(TAG, "user lookup error");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
