package com.example.alex.wifidirectbusinesscards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;


public class ViewCard extends Activity {
    List<VCard> vcardList = new ArrayList<VCard>();
    String pS;
    int filePosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_card);
        String fName = getIntent().getStringExtra("FILE_NAME");
        pS = getIntent().getStringExtra("POSITION");
        Log.e("ViewCard", fName+"///////////");
        File tempFile = new File(getFilesDir()+"//"+fName);
        try {
            vcardList = Ezvcard.parse(tempFile).all();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if (pS.equals("GO_TO_END"))
        {
            filePosition = vcardList.size()-1;
        }
        else
        {
            filePosition = Integer.parseInt(pS);
        }
        TextView t = (TextView)findViewById(R.id.cardEmail);
        t.setText(vcardList.get(filePosition).getEmails().get(0).getValue());
        t = (TextView)findViewById(R.id.cardName);
        t.setText(vcardList.get(filePosition).getFormattedName().getValue());
        t = (TextView)findViewById(R.id.cardTitle);
        t.setText(vcardList.get(filePosition).getTitles().get(0).getValue());
        t = (TextView)findViewById(R.id.cardCompany);
        t.setText(vcardList.get(filePosition).getOrganization().getValues().get(0));
        t = (TextView)findViewById(R.id.cardWork);
        t.setText(vcardList.get(filePosition).getTelephoneNumbers().get(0).getText()+"(Work)");
        t = (TextView)findViewById(R.id.cardCell);
        t.setText(vcardList.get(filePosition).getTelephoneNumbers().get(1).getText()+"(Cell)");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
