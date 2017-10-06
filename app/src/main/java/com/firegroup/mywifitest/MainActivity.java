package com.firegroup.mywifitest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private Activity mActivity;
        WifiP2pManager.PeerListListener mPeerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                peers = new ArrayList<WifiP2pDevice>();
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());
                peername = new String[peers.size()];
                if(peers.size()==0)
                {
                    if(peername.length>0) {
                        peername = new String[1];
                        peername[0] = "No device2";
                    }
                    else{
                        peername = new String[1];
                        peername[0] = "No device3";
                    }
                }
                else{
                    int i = 0;
                    for (WifiP2pDevice device : peers) {
                        peername[i++] = device.deviceName;
                    }
                }
                picker.post(new Runnable() {
                    @Override
                    public void run() {
                        picker.setDisplayedValues(peername);
                    }
                });
            }
        };

        public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                           Activity activity) {
            super();
            this.mManager = manager;
            this.mChannel = channel;
            this.mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Toast.makeText(context,"Wifi is enabled!",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context,"Please make sure the wifi is open!",Toast.LENGTH_LONG).show();
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                if(mManager != null)
                {
                    mManager.requestPeers(mChannel,mPeerListListener);
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
            }
        }
    }

    public void connect(final int num) {
        // Picking the first device found on the network.
        WifiP2pDevice device = peers.get(num);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(),"Conncet sigao",Toast.LENGTH_LONG);
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(),"Failed to connect:"+Integer.toString(reason),Toast.LENGTH_LONG);
            }
        });
    }

    ArrayList<WifiP2pDevice> peers;
    String[] peername;
    NumberPicker picker;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    String host = "";
    EditText message;
    TextView ipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        picker = (NumberPicker)findViewById(R.id.pick);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int num = picker.getValue();
                Toast.makeText(getApplicationContext(),"You have select the "+ Integer.toString(num),Toast.LENGTH_LONG).show();
                connect(num);
            }
        });
        peername = new String[1];
        peername[0] = "No Device";
        picker.setDisplayedValues(peername);
        Button commit = (Button)findViewById(R.id.commit);
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText hosts = (EditText)findViewById(R.id.host);
                host = hosts.getText().toString();
            }
        });
        Button server = (Button)findViewById(R.id.server);
        Button client = (Button)findViewById(R.id.client);
        Button show = (Button)findViewById(R.id.show);
        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ServerWifi().start();
            }
        });

        client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ClientWifi().start();
            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new showIP().start();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(),"Success in wifi discover!",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reasonCode) {
            }
        });
        message = (EditText)findViewById(R.id.message);
        ipt = (TextView)findViewById(R.id.ip);
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private class ClientWifi extends Thread{
        @Override
        public void run(){
            try {
                Socket socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(host,8000)),1000);
                //2.获取输出流，向服务器端发送信息
                OutputStream os = socket.getOutputStream();
                String messages = message.getText().toString();
                os.write(messages.getBytes());
                socket.shutdownOutput();
                socket.close();
            }catch (IOException e){
                Toast.makeText(getApplicationContext(),"Failed to send message!",Toast.LENGTH_LONG).show();
            }
        }
    }

    private class showIP extends Thread{
        @Override
        public void run(){
            try {
                InetAddress address = InetAddress.getLocalHost();
                final String ip = address.getHostAddress();
                ipt.post(new Runnable() {
                    @Override
                    public void run() {
                        ipt.setText(ip);
                    }
                });
            }catch (IOException e){}
        }
    }

    private class ServerWifi extends Thread{
        @Override
        public void run(){
            try {
                ServerSocket serverSocket = new ServerSocket(8000);
                Socket socket = null;
                socket = serverSocket.accept();
                Toast.makeText(getApplicationContext(),"create Host saigao",Toast.LENGTH_LONG).show();
                //3.连接后获取输入流，读取客户端信息
                InputStream is=null;
                BufferedReader br=null;
                OutputStream os=null;
                PrintWriter pw=null;
                is = socket.getInputStream();     //获取输入流
                InputStreamReader isr = new InputStreamReader(is,"UTF-8");
                br = new BufferedReader(isr);
                String info = null;
                while((info=br.readLine())!=null){//循环读取客户端的信息
                    final String infor = info;
                    message.post(new Runnable() {
                        @Override
                        public void run() {
                            message.setText(infor);
                        }
                    });
                }
                socket.shutdownInput();//关闭输入流
                socket.close();
            }catch (IOException e){}
        }
    }
}
