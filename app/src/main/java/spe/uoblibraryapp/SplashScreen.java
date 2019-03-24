package spe.uoblibraryapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import spe.uoblibraryapp.api.ncip.WMSNCIPService;

public class SplashScreen extends AppCompatActivity {

    private final String TAG = "SplashScreen";
    private MyBroadCastReceiver myBroadCastReceiver;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    Thread splashTread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide title
        getSupportActionBar().hide(); // Hide the title bar
        setContentView(R.layout.activity_splash_screen);
        StartAnimations();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.IntentActions.LOOKUP_USER_RESPONSE);
        intentFilter.addAction(Constants.IntentActions.LOOKUP_USER_ERROR);
        intentFilter.addAction(Constants.IntentActions.ACCESS_TOKEN_ERROR);
        myBroadCastReceiver = new MyBroadCastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(myBroadCastReceiver, intentFilter);
    }

    private void StartAnimations() {
        Animation loadAnimation = AnimationUtils.loadAnimation(this, R.anim.alpha_anim);
        loadAnimation.reset();
        LinearLayout linearLayout = findViewById(R.id.lin_lay);
        linearLayout.clearAnimation();
        linearLayout.startAnimation(loadAnimation);

        loadAnimation = AnimationUtils.loadAnimation(this, R.anim.translate_anim);
        loadAnimation.reset();
        ImageView imageView = findViewById(R.id.splash);
        imageView.clearAnimation();
        imageView.startAnimation(loadAnimation);

        SharedPreferences prefs = getSharedPreferences("userDetails", MODE_PRIVATE);
        if (prefs.getString("principalID", "").equals("")) {
            Log.e("DENIS", "CREATE_IF_1");
            splashTread = new Thread() {
                @Override
                public void run() {
                    try {
                        int waited = 0;
                        while (waited < 1500) {
                            sleep(100);
                            waited += 100;
                        }
                        Intent intent;
                        intent = new Intent(SplashScreen.this,
                                ActivityHome.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        SplashScreen.this.finish();
                    } catch (InterruptedException e) {

                    } finally {
                        SplashScreen.this.finish();
                    }
                }
            };
            splashTread.start();
        } else{
            Log.e("DENIS", "CREATE_ELSE_1");
            Intent getUserProfileIntent = new Intent(Constants.IntentActions.LOOKUP_USER);
            Log.e("DENIS", "CREATE_ELSE_2");
            WMSNCIPService.enqueueWork(getApplicationContext(), WMSNCIPService.class, WMSNCIPService.jobId, getUserProfileIntent);
            Log.e("DENIS", "CREATE_ELSE_3");
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(myBroadCastReceiver);
    }

    class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("DENIS", "onReceive() called.");
            try {
                Log.d(TAG, "onReceive() called");
                if (Constants.IntentActions.LOOKUP_USER_RESPONSE.equals(intent.getAction())) {
                    Log.e("DENIS", "No Error.");
                    Intent startActivityIntent = new Intent(SplashScreen.this, ActivityHome.class);
                    startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(startActivityIntent);
                    SplashScreen.this.finish();
                } else {
                    //No internet detected.
                    //TODO: Try again.
                    Log.e("DENIS", "Error.");

                    if (!isNetworkConnected()){
                        Toast.makeText(getApplicationContext(), "Connect to the internet & try again.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else{
                        //check if it still works. -> SSO could be down.
                        while (true) {
                            Toast.makeText(getApplicationContext(), "Internet Not ", Toast.LENGTH_LONG).show();
                        }
                    }
                }

            } catch (Exception ex) {
                Log.e("DENIS", "Exception Error.");
                ex.printStackTrace();
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}