package spe.uoblibraryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import spe.uoblibraryapp.api.WMSException;
import spe.uoblibraryapp.api.WMSResponse;
import spe.uoblibraryapp.api.ncip.WMSNCIPElement;
import spe.uoblibraryapp.api.ncip.WMSNCIPResponse;
import spe.uoblibraryapp.api.wmsobjects.WMSCheckout;
import stanford.androidlib.SimpleActivity;

public class ActivityConfirm extends SimpleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);


        Intent intent = getIntent();

        String xml = intent.getStringExtra("xml");

        try {
            WMSResponse response = new WMSNCIPResponse(xml);

            if (response.didFail()) {
                throw new WMSException("There was an error retrieving the User Profile");
            }
            Document doc;
            try {
                doc = response.parse();
            } catch (IOException | SAXException | ParserConfigurationException e) {
                throw new WMSException("There was an error Parsing the WMS response");
            }
            Node node = doc.getElementsByTagName("ns1:LookupUserResponse").item(0);
            WMSCheckout checkout = new WMSCheckout(new WMSNCIPElement(node), "userId goes here");


            TextView bookName = findTextView(R.id.book_name_confirm);
            TextView bookAuthor = findTextView(R.id.book_author_confirm);

            bookName.setText(checkout.getBook().getTitle());
            bookAuthor.setText(checkout.getBook().getAuthor());



            Button scanButton = findButton(R.id.scan_button);
            Button closeButton = findButton(R.id.close_button);

            scanButton.setOnClickListener((view) -> {
                startActivity(new Intent(getApplicationContext(), ActivityScanNFC.class));
            });
            closeButton.setOnClickListener((view) -> {
                startActivity(new Intent(getApplicationContext(), ActivityHome.class));
            });



        } catch (Exception ex){
            // TODO fix this
            // Cry
        }






    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
    }
}
