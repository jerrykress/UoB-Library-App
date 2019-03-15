package spe.uoblibraryapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;

import spe.uoblibraryapp.api.ncip.WMSNCIPService;
import spe.uoblibraryapp.nfc.BarcodeException;
import spe.uoblibraryapp.nfc.CheckedOutException;
import spe.uoblibraryapp.nfc.IntentException;
import spe.uoblibraryapp.nfc.NFC;
import spe.uoblibraryapp.nfc.NFCTechException;

public class ActivityScanNFC extends AppCompatActivity {
    private static final String TAG = "Scan NFC Activity";

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private String[][] techList;
    private Dialog scanDialog;
    private MyBroadCastReceiver myBroadCastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        myBroadCastReceiver = new MyBroadCastReceiver();
        setContentView(R.layout.activity_home_nfc);
        Activity myAct = this;



        if (nfcAdapter == null){
            Toast.makeText(this, "NFC Not Supported. Functionality Disabled.", Toast.LENGTH_LONG).show();
            finish();
            return;
            //TODO: Disable Floating Button & Scan.
        }
        else {
            if (nfcAdapter.isEnabled()){
                //Do NFC Stuff.
                Intent pnd = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(this, 0, pnd, 0);
                // Setup a tech list for NfcV tag.
                techList = new String[][]{ new String[]{NfcV.class.getName()} };
            }
            else{
                //disabled.
                Toast.makeText(this, "Please enable NFC & try again.", Toast.LENGTH_LONG).show();
                Intent i = new Intent(Settings.ACTION_NFC_SETTINGS);
                startActivity(i);
                finish();
            }
        }


        Button butt = findViewById(R.id.btnShowMeHow);
        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityScanNFC.ViewDialog alert = new ActivityScanNFC.ViewDialog();
                //alert.showDialog(myAct, R.layout.dialog_problems_scanning_layout, true);
               // ActivityScanNFC.ViewDialog alert = new ActivityScanNFC.ViewDialog();
                alert.showDialog(myAct, R.layout.dialog_scan_select_library, false);


            }
        });

    }

    /**
     * Called when an intent gets passed to the activity,
     * hopefully an NFC tag.
     * @param scanIntent - an intent sent to the activity, hopefully a tag
     */
    @Override
    protected void onNewIntent(Intent scanIntent) {
        ProgressDialog nDialog;
        nDialog = new ProgressDialog(this);
        nDialog.setMessage("Loading...");
        nDialog.setTitle("Checkout in progress");
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
        scanDialog = nDialog;

        ActivityScanNFC activityScanNFC = this;


        try {
            NFC nfc = new NFC(scanIntent);
            // Tag has been scanned now stop scanning for tags
            //nfcAdapter.disableForegroundDispatch(ActivityScanNFC.this);

            // Send intent to WMSNCIPService with itemId
            Intent checkoutIntent = new Intent(Constants.IntentActions.CHECKOUT_BOOK);
            checkoutIntent.putExtra("itemId", nfc.getBarcode());
            WMSNCIPService.enqueueWork(getApplicationContext(), WMSNCIPService.class, 1000, checkoutIntent);
            // When checkout is complete the confirm activity is started by the WMSNCIPService.

        } catch (NFCTechException e) {
            e.printStackTrace();
            nDialog.setMessage("Not the right NFC/RFID type");
            Log.d(TAG, "Not the right NFC/RFID type");
        } catch (IntentException e) {
            e.printStackTrace();
            Log.d(TAG, "No tag in the intent");
        } catch (IOException e) {
            e.printStackTrace();
            nDialog.setMessage("There was a problem with the tag. Hold phone still over tag.");
            Log.d(TAG, "Can't connect to the tag");
        } catch (BarcodeException e) {
            e.printStackTrace();
            nDialog.setMessage("There was a problem reading the tag");
            Log.d(TAG, "There was a problem with the tag");
        } catch (CheckedOutException e) {
            Log.e(TAG, "Book checked out");
            Toast.makeText(activityScanNFC, "Book checked out already", Toast.LENGTH_LONG).show();
            nDialog.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, techList);
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.IntentActions.BOOK_CHECK_OUT_RESPONSE);
            registerReceiver(myBroadCastReceiver, intentFilter);
            Log.d(TAG, "Receiver Registered");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        nfcAdapter.disableForegroundDispatch(this);
        if (scanDialog != null){
            scanDialog.dismiss();
        }
        unregisterReceiver(myBroadCastReceiver);
        super.onPause();
    }

    public void onBackPressed(){
        Intent intent = new Intent(ActivityScanNFC.this, ActivityHome.class);
        startActivity(intent);
        finish();
    }

    /**
     * turns an array of bytes into hex representation
     * @param src - bytes to turn into hex
     * @return - the hex representation of the bytes as a string
     */
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");

        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];

        for (byte aSrc : src) {
            buffer[0] = Character.forDigit((aSrc >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(aSrc & 0x0F, 16);

            stringBuilder.append(buffer);
        }
        return stringBuilder.toString().toUpperCase().replace('X','x');
    }



    class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d(TAG, "onReceive() called");

                String xml = intent.getStringExtra("xml");
                Intent confirmIntent = new Intent(getApplicationContext(), ActivityConfirm.class);
                confirmIntent.putExtra("xml", xml);
                startActivity(confirmIntent);
                finish();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    //Problems Scanning Dialog
    public class ViewDialog {

        public void showDialog(Activity activity, int layoutResID, boolean setCanceledOnTouchOutside){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(setCanceledOnTouchOutside);
            dialog.setContentView(layoutResID);

            if (layoutResID == R.layout.dialog_scan_select_library) {
                //Library Selection
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                Spinner spinnerSelect = dialog.findViewById(R.id.spinnerSelectLibrary);

                SharedPreferences pref = getApplicationContext().getSharedPreferences("spinnerSelection", Context.MODE_PRIVATE);

                if (pref.contains("spinnerSelect"))
                    Log.e("YES", "YES");
                    //SET SELECTED ITEM TO THAT INDEX.
                else
                    pref.edit().putInt("spinnerSelect", 0).apply();



                ((Button) dialog.findViewById(R.id.btnConfirm)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Remember me for next time.
                        pref.edit().putInt("spinnerSelect", spinnerSelect.getSelectedItemPosition()).apply();
                        
                        // Close dialog
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }
    }
}