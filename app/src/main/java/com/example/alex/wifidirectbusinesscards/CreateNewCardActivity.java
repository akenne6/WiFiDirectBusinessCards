package com.example.alex.wifidirectbusinesscards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.FormattedName;
import ezvcard.property.Organization;


public class CreateNewCardActivity extends Activity {
    private String name = "";
    private String mobilePhoneNumber = "";
    private String workPhoneNumber = "";
    private String emailAddress = "";
    private String title = "";
    private String company = "";
    private String FILE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FILE_NAME = getIntent().getStringExtra("FILE_NAME");
        setContentView(R.layout.activity_create_new_card);
        Button b = (Button) findViewById(R.id.submitCardInfo);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText temp = (EditText)findViewById(R.id.editName);
                name = temp.getText().toString();
                temp = (EditText)findViewById(R.id.editPhone1);
                workPhoneNumber = temp.getText().toString();
                temp = (EditText)findViewById(R.id.editPhoneCell);
                mobilePhoneNumber = temp.getText().toString();
                temp = (EditText)findViewById(R.id.editEmail);
                emailAddress = temp.getText().toString();
                temp = (EditText)findViewById(R.id.editJobTitle);
                title = temp.getText().toString();
                temp = (EditText)findViewById(R.id.editCompany);
                company = temp.getText().toString();

                putInVCard();

                Intent intent = new Intent(v.getContext(), ViewCard.class);
                intent.putExtra("FILE_NAME", FILE_NAME);
                intent.putExtra("POSITION", "GO_TO_END");
                startActivity(intent);
            }
        });
    }

    public void putInVCard()
    {
        VCard vC = new VCard();
        vC.setFormattedName(name);
        vC.addTitle(title);
        vC.setOrganization(company);
        vC.addTelephoneNumber(workPhoneNumber,TelephoneType.WORK);
        vC.addTelephoneNumber(mobilePhoneNumber, TelephoneType.CELL);
        vC.addEmail(emailAddress);
        FileOutputStream fOS = null;
        try {
            fOS = openFileOutput(FILE_NAME, MODE_APPEND);
            Ezvcard.write(vC).go(fOS);
            Log.e("CreateNewCardActivity", "Writing complete");
            fOS.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_new_card, menu);
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
