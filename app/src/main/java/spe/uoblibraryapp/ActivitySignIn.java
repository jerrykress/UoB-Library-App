package spe.uoblibraryapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import stanford.androidlib.SimpleActivity;


public class ActivitySignIn extends SimpleActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("Single Sign On");

        WebView mywebview = findViewById(R.id.loginWebView);
        mywebview.getSettings().setJavaScriptEnabled(true);
        mywebview.getSettings().setLoadWithOverviewMode(true);
        mywebview.getSettings().setUseWideViewPort(true);
        mywebview.setInitialScale(1);
        mywebview.loadUrl("https://authn.sd00.worldcat.org/oauth2/authorizeCode?client_id=LRQvSrRL1pjZCy8R0AyQpL45QtYvJs6SpjKSF2EmqzmVc0mpIhE85ahM2m4XbByK9qMhl9IcX8fOeOet&authenticatingInstitutionId=132607&contextInstitutionId=132607&redirect_uri=uoblibrary%3A%2F%2Fauthenticate&response_type=token&scope=WMS_NCIP%20refresh_token");
        mywebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String URL = request.getUrl().toString();
                if (URL == null || URL.startsWith("http://") || URL.startsWith("https://"))
                    return false;
                else {
                    // TODO need to change this... never actually checks if the url received is the url expected.
                    if (!isAuthorisationDenied(URL)) {
                        processAuthorisationString(URL);
                        /*Successful*/
                        Toast.makeText(getApplicationContext(), "Sign In Successful", Toast.LENGTH_SHORT).show();
                        finish();
                        return true;
                    }
                    else {
                        /*User Denied Request*/
                        Toast.makeText(getApplicationContext(), "Authentication Failed", Toast.LENGTH_SHORT).show();
                        finish();
                        //I guess try again...
                        return true;
                    }
                }
            }
        });

    }

    private boolean isAuthorisationDenied(String s){
        return s.contains("uoblibrary://authenticate#error");
    }
    private void processAuthorisationString(String s){

        // TODO: Could this be a loop? store is a hash table, that way its future proof if they change the order of url arguments
        int startOfToken = s.indexOf("access_token=") + 13;
        int endOfToken = s.indexOf("&principalID=");
        String authorisationToken = s.substring(startOfToken, endOfToken);

        int startOfPrincipalID = endOfToken + 13;
        int endOfPrincipalID = s.indexOf("&principalIDNS");
        String principalID = s.substring(startOfPrincipalID, endOfPrincipalID);

        int startOfExpires_at = s.indexOf("&expires_at=") + 12;
        int endOfExpires_at = s.indexOf("Z&refresh_token=");
        String authorisationTokenExpiry = s.substring(startOfExpires_at, endOfExpires_at);

        int startOfRefreshToken = endOfExpires_at + 16;
        int endOfRefreshToken = s.indexOf("&refresh_token_expires_in=");
        String refreshToken = s.substring(startOfRefreshToken, endOfRefreshToken);

        int startOfRefreshTokenExpires_at = s.indexOf("&refresh_token_expires_at=") + 26;
        int endOfRefreshTokenExpires_at = s.length()-1;
        String refreshTokenExpiry = s.substring(startOfRefreshTokenExpires_at, endOfRefreshTokenExpires_at);

        //------put in sharedPreferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        pref.edit().putString("authorisationToken", authorisationToken).apply();
        pref.edit().putString("authorisationTokenExpiry", authorisationTokenExpiry).apply();
        pref.edit().putString("principalID", principalID).apply();
        pref.edit().putString("refreshToken", refreshToken).apply();
        pref.edit().putString("refreshTokenExpiry", refreshTokenExpiry).apply();
        /*

          String tag = "STRING PROCESSING";
          Log.i(tag, "Access Token: " + authorisationToken);
          Log.i(tag, "Principal ID: " + principalID);
          Log.i(tag, "Auth Expires At: " + authorisationTokenExpiry);
          Log.i(tag, "Refresh Token: " + refreshToken);
          Log.i(tag, "Refresh Expires At: " + refreshTokenExpiry);

        */
    }
}
