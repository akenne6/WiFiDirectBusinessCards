package com.example.alex.wifidirectbusinesscards;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.xml.XCardDocument;
import ezvcard.parameter.TelephoneType;


public class MainActivity extends Activity implements Main_Fragment.OnMainFragmentInteractionListener,
ViewCardsListFragment.OnViewCardsFragmentInteractionListener, CreateNewCardFragment.OnCreateCardFragmentInteractionListener,
P2pConnectionFragment.OnP2pFragmentInteractionListener, WiFiDirectServicesList.DeviceClickListener,
WifiP2pManager.ConnectionInfoListener{

    private static final String FILE_NAME = "vcards.xml";
    private String CONNECT_TYPE;
    private static final int SERVER_PORT = 4545;
    private FileOutputStream fOS;
    private VCard document;
    private static List<VCard> vcardList;
    private P2pConnectionFragment pFragment;
    private static FragmentManager fM;
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
            vcardList = Ezvcard.parse(new File(getFilesDir(), FILE_NAME)).all();
            Log.e("ViewCardListActivity: SIZE: ", vcardList.size()+"");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        fM = getFragmentManager();
        getFragmentManager().beginTransaction().add(R.id.container_main, new Main_Fragment()).commit();
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

    @Override
    public void onConnect(String s) {
        Bundle temp = new Bundle();
        CONNECT_TYPE = s;
        temp.putString("CONNECT_TYPE", s);
        P2pConnectionFragment fragment = new P2pConnectionFragment();
        fragment.setArguments(temp);
        FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.container_main, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onViewCardButton() {
        ViewCardsListFragment fragment = new ViewCardsListFragment();
        Bundle temp = new Bundle();
        temp.putString("FILE_NAME", FILE_NAME);
        temp.putString("INCOMING_ACTIVITY", "MainActivity");
        fragment.setArguments(temp);
        FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.container_main, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    @Override
    public void onCreateButton() {
        CreateNewCardFragment fragment = new CreateNewCardFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.container_main, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCardSelected(String file, int position, String incomingLocation) {
        if (incomingLocation.equals("P2pConnectionFragment"))
        {
            Log.d("MA", "Made it to the main activity");
            new FileServerAsyncTask(this).execute(position);
        }
        else
        {
            ViewCardFragment fragment = new ViewCardFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.container_main, fragment);
            Bundle temp = new Bundle();
            temp.putString("POSITION", position+"");
            temp.putString("FILE_NAME", FILE_NAME);
            fragment.setArguments(temp);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    public void onCardCreated(String name, String workNumber, String cellNumber, String email, String title, String company) {
        String[] data = {name, workNumber, cellNumber, email, title, company};
        putInVCard(data);

        ViewCardFragment fragment = new ViewCardFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.container_main, fragment);
        Bundle temp = new Bundle();
        temp.putString("POSITION", "GO_TO_END");
        temp.putString("FILE_NAME", FILE_NAME);
        fragment.setArguments(temp);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void putInVCard(String[] data)
    {
        VCard vC = new VCard();
        vC.setFormattedName(data[0]);
        vC.addTitle(data[1]);
        vC.setOrganization(data[2]);
        vC.addTelephoneNumber(data[3], TelephoneType.WORK);
        vC.addTelephoneNumber(data[4], TelephoneType.CELL);
        vC.addEmail(data[5]);
        vcardList.add(vC);
        try {
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
    public void startSendingFile() {
        ViewCardsListFragment fragment = new ViewCardsListFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.container_main, fragment);
        Bundle temp = new Bundle();
        temp.putString("INCOMING_ACTIVITY", "P2pConnectionFragment");
        fragment.setArguments(temp);
        transaction.commit();
    }

    @Override
    public void setP2pFragment(P2pConnectionFragment p) {
        pFragment = p;
    }

    @Override
    public void connectP2p(WiFiP2pService wifiP2pService) {
        pFragment.connectP2p(wifiP2pService);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        pFragment.onConnectionInfoAvailable(info);
    }

    public static class FileServerAsyncTask extends AsyncTask<Integer, Void, String> {

        private Context context;
        private TextView statusText;

        /**
         *
         */
        public FileServerAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Integer... params) {
            try {
                while(true) {
                    Log.d("MA", "About to start Server");
                    ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
                    Log.d("p2p", "Server: Socket opened");
                    Socket client = serverSocket.accept();
                    Log.d("p2p", "Server: connection done");
                    OutputStream os = client.getOutputStream();
                    InputStream is = null;
                    String text = Ezvcard.write(vcardList.get(params[0])).go();
                    is = new ByteArrayInputStream(text.getBytes());
                    copyFile(is, os);
                    serverSocket.close();
                    client.close();
                    return "Done";
                }
            } catch (IOException e) {
                Log.e("p2p", e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            Log.e("p2p", "Done with the file transfer. Look at for a toast.");
            if (result != null) {
                Toast.makeText(context.getApplicationContext(), "Download has Finished", Toast.LENGTH_LONG).show();
            }
            Main_Fragment fragment = new Main_Fragment();
            fM.beginTransaction().replace(R.id.container_main, fragment).commit();
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d("p2p", e.toString());
            return false;
        }
        return true;
    }

    public void onFileReceived()
    {
        Main_Fragment fragment = new Main_Fragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.container_main, fragment);
        transaction.commit();
    }


}
