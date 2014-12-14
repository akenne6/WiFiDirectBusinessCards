package com.example.alex.wifidirectbusinesscards;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;


public class P2pConnectionFragment extends Fragment {
    private OnP2pFragmentInteractionListener mListener;
    private String CONNECT_TYPE = "";
    private View mainView;

    public static final String TAG = "wifidirectdemo";
    public static int CARD_NUMBER;
    private WifiP2pInfo info;

    // TXT RECORD properties
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    private WifiP2pManager manager;

    static final int SERVER_PORT = 4545;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private WiFiDirectServicesList servicesList;

    private TextView statusTxtView;
    private TextView progressTxtView;
    private InetAddress mAddress;
    private ServerSocket socket = null;

    private MainActivity mActivity;


    public P2pConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume()
    {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, getActivity());
        getActivity().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            CONNECT_TYPE = getArguments().getString("CONNECT_TYPE");
            mActivity = (MainActivity)getActivity();
        }
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) mActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(mActivity, mActivity.getMainLooper(), null);
        startRegistrationAndDiscovery();
        if(CONNECT_TYPE.equals("SEND")) {
            servicesList = new WiFiDirectServicesList();
            getFragmentManager().beginTransaction()
                    .add(R.id.container_root, servicesList, "services").commit();
        }
        mListener.setP2pFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(CONNECT_TYPE.equals("SEND"))
        {
            mainView = inflater.inflate(R.layout.fragment_p2p_connection_send, container, false);
            statusTxtView = (TextView)mainView.findViewById(R.id.status_text);
            mainView.findViewById(R.id.cancelConnectSend).setOnClickListener(onClickListener);
            mainView.findViewById(R.id.sendContactCard).setOnClickListener(onClickListener);
        }
        else
        {
            mainView = inflater.inflate(R.layout.fragment_p2p_connection_receive, container, false);
            mainView.findViewById(R.id.cancelConnectReceive).setOnClickListener(onClickListener);
            progressTxtView = (TextView)mainView.findViewById(R.id.receive_status_text);
        }

        return mainView;
    }

    private void startRegistrationAndDiscovery() {
        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        manager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                if(CONNECT_TYPE.equals("SEND")) {
                    appendStatus("Added Local Service");
                }
                else
                {
                    Log.d(TAG, "Added Local Service");
                }
            }

            @Override
            public void onFailure(int error) {
                if(CONNECT_TYPE.equals("SEND")) {
                    appendStatus("Failed to add a service");
                }
                else
                {
                    Log.d(TAG, "Failed to add a service");
                }
            }
        });

        discoverService();

    }

    private void discoverService() {

        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */
        if(CONNECT_TYPE.equals("SEND")) {
            manager.setDnsSdResponseListeners(channel,
                    new WifiP2pManager.DnsSdServiceResponseListener() {

                        @Override
                        public void onDnsSdServiceAvailable(String instanceName,
                                                            String registrationType, WifiP2pDevice srcDevice) {

                            // A service has been discovered. Is this our app?

                            if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {

                                // update the UI and add the item the discovered
                                // device.
                                WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager()
                                        .findFragmentByTag("services");
                                if (fragment != null) {
                                    WiFiDirectServicesList.WiFiDevicesAdapter adapter = ((WiFiDirectServicesList.WiFiDevicesAdapter) fragment
                                            .getListAdapter());
                                    WiFiP2pService service = new WiFiP2pService();
                                    service.device = srcDevice;
                                    service.instanceName = instanceName;
                                    service.serviceRegistrationType = registrationType;
                                    adapter.add(service);
                                    adapter.notifyDataSetChanged();
                                    Log.d(TAG, "onBonjourServiceAvailable "
                                            + instanceName);
                                }
                            }

                        }
                    }, new WifiP2pManager.DnsSdTxtRecordListener() {

                        /**
                         * A new TXT record is available. Pick up the advertised
                         * buddy name.
                         */
                        @Override
                        public void onDnsSdTxtRecordAvailable(
                                String fullDomainName, Map<String, String> record,
                                WifiP2pDevice device) {
                            Log.d(TAG,
                                    device.deviceName + " is "
                                            + record.get(TXTRECORD_PROP_AVAILABLE));
                        }
                    });

            // After attaching listeners, create a service request and initiate
            // discovery.
        }
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        if(CONNECT_TYPE.equals("SEND")) {
                            appendStatus("Added service discovery request");
                        }
                        else
                        {
                            Log.d(TAG, "Added service discovery request");
                        }
                    }

                    @Override
                    public void onFailure(int arg0) {
                        if(CONNECT_TYPE.equals("SEND")) {
                            appendStatus("Failed adding service discovery request");
                        }
                        else
                        {
                            Log.d(TAG, "Failed adding service discovery request");
                        }
                    }
                });
            manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    if (CONNECT_TYPE.equals("SEND")) {
                        appendStatus("Service discovery initiated");
                    } else {
                        Log.d(TAG, "Service discovery initiated");
                    }
                }

                @Override
                public void onFailure(int arg0) {
                    if (CONNECT_TYPE.equals("SEND")) {
                        appendStatus("Service discovery failed");
                    } else {
                        Log.d(TAG, "Service discovery failed");
                }

            }
        });
    }

    public void appendStatus(String status) {
        String current = statusTxtView.getText().toString();
        statusTxtView.setText(current + "\n" + status);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.sendContactCard:
                {
                    sendCardButton();
                    break;
                }
                case R.id.cancelConnectReceive: {
                    cancelReceiveButton();
                    break;
                }
                case R.id.cancelConnectSend: {
                    cancelSendButton();
                    break;
                }
                default:
                    break;
            }
        }
    };
    public void sendCardButton()
    {
        if (serviceRequest != null) {
            manager.removeServiceRequest(channel, serviceRequest,
                    new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Service Request Removed");
                        }

                        @Override
                        public void onFailure(int arg0) {
                            Log.e(TAG, "Service Request Remove Failed");
                        }
                    });
        }
        mListener.startSendingFile();
    }

    public void cancelReceiveButton()
    {
        TextView text = (TextView)mainView.findViewById(R.id.receive_status_text);
        text.setText("You clicked Cancel");
    }

    public void cancelSendButton()
    {
        TextView text =(TextView)mainView.findViewById(R.id.status_text);
        text.setText("You clicked Cancel");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnP2pFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        //Do things depending on type of connection
        this.info = info;

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
            //Do nothing for now, allow user to select another connection
            Log.d("Name", info.describeContents()+"");
        } else if (info.groupFormed) {
            if (serviceRequest != null) {
                manager.removeServiceRequest(channel, serviceRequest,
                        new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Service Request Removed");
                            }

                            @Override
                            public void onFailure(int arg0) {
                                Log.e(TAG, "Service Request Remove Failed");
                            }
                        });
            }
            progressTxtView.setText("Connection");
            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, "vcards.xml");
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, SERVER_PORT);
            getActivity().startService(serviceIntent);

            mActivity.onFileReceived();

        }
    }

    public void connectP2p(WiFiP2pService wifiP2pService) {
        //Make connection
        WifiP2pConfig config = new WifiP2pConfig();
        if(CONNECT_TYPE.equals("SEND")) {
            config.groupOwnerIntent = 15;
        }
        else
        {
            config.groupOwnerIntent = 0;
        }
        config.deviceAddress = wifiP2pService.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                if(CONNECT_TYPE.equals("SEND")) {
                    appendStatus("Connecting to service");
                }
                else {
                    Log.d(TAG, "Connecting to service");
                }
            }

            @Override
            public void onFailure(int errorCode) {
                if(CONNECT_TYPE.equals("SEND")) {
                    appendStatus("Failed connecting to service");
                }
                else {
                    Log.e(TAG, "Failed connecting to service");
                }
            }
        });
    }
    @Override
    public void onStop()
    {
        super.onStop();
        manager = null;
        channel = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnP2pFragmentInteractionListener {
        // TODO: Update argument type and name
        public void startSendingFile();
        public void setP2pFragment(P2pConnectionFragment p);
    }

}
