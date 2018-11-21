package spe.uoblibraryapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import spe.uoblibraryapp.nfc.IntentException;
import spe.uoblibraryapp.nfc.NFC;
import spe.uoblibraryapp.nfc.NFCTechException;

public class HomeNFC extends AppCompatActivity {


    TextView txtContent;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private String[][] mTechList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_nfc);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (!(nfcAdapter != null && nfcAdapter.isEnabled())){
            Toast.makeText(this, "No NFC Detected", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            txtContent = findViewById(R.id.txtContent);
            Intent pnd = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, pnd, 0);
            // Setup a tech list for NfcV tag.
            mTechList = new String[][]{
                    new String[]{NfcV.class.getName()},
            };
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Toast.makeText(this, "NFC Intent", Toast.LENGTH_SHORT).show();

        NFC nfc = new NFC();

        try {
            nfc.setNfcTag(intent);
        } catch (NFCTechException e) {

        } catch (IntentException e) {

        } catch (IOException e) {

        }

        try{
            txtContent.setText( nfc.getSystemInfo().toString()  );
        } catch(IOException e){
            txtContent.setText( "Error: Exception Thrown" );
        } catch (IntentException e) {

        }
    }


    @Override
    protected void onResume() {
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, mTechList);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }


}
