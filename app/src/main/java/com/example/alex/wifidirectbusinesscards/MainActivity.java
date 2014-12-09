package com.example.alex.wifidirectbusinesscards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import ezvcard.VCard;
import ezvcard.io.xml.XCardDocument;


public class MainActivity extends Activity{

    private static final String FILE_NAME = "vcards.xml";
    private FileOutputStream fOS;
    private VCard document;
    private List<VCard> vcardList;
    private File f;
    private final String tempVCard =
            "BEGIN:VCARD\r\n"+
            "VERSION:4.0\r\n"+
            "FN:Forrest Gump\r\n"+
            "ORG:Bubba Gump Shrimp Co.\r\n"+
            "TITLE:Shrimp Man\r\n"+
            "TEL;TYPE=work,voice;VALUE=uri:tel:+1-111-555-1212\r\n"+
            "TEL;TYPE=cell,voice;VALUE=uri:tel:+1-404-555-1212\r\n"+
            "EMAIL:forrestgump@example.com\r\n"+
            "REV:20080424T195243Z\r\n"+
            "END:VCARD\r\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            fOS = openFileOutput(FILE_NAME, MODE_APPEND);
            //fOS.write(tempVCard.getBytes());
            fOS.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void ConnectDevices(View v)
    {
        Intent intent = new Intent(this, P2pConnectionActivity.class);
        startActivity(intent);
    }
    public void ViewCardsList(View v)
    {
        Intent intent = new Intent(this, ViewCardsListActivity.class);
        intent.putExtra("FILE_NAME", FILE_NAME);
        intent.putExtra("INCOMING_ACTIVITY", "MainActivity");
        startActivity(intent);
    }
    public void CreateCard(View v)
    {
        Intent intent = new Intent(this, CreateNewCardActivity.class);
        intent.putExtra("FILE_NAME", FILE_NAME);
        startActivity(intent);
    }
}
