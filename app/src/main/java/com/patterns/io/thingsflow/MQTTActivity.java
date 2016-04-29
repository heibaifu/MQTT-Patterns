package com.patterns.io.thingsflow;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.patterns.io.thingsflow.MQTTConnectFragment.ConnectDataPassListener;
import com.patterns.io.thingsflow.MQTTPublishFragment.PublishDataPassListener;
import com.patterns.io.thingsflow.MQTTSubscribeFragment.SubscribeDataPassListener;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.InputStream;

public class MQTTActivity extends AppCompatActivity implements ConnectDataPassListener, PublishDataPassListener, MqttCallback, SubscribeDataPassListener  {

     private static final int PICKFILE_RESULT_CODE = 1;

    public String                   topicToPublish;
    public String                   topicToSubscribe;
    public String                   content;
    public String                   MQTTmessage;
    public String                   broker;
    public String                   port;
    public String                   clientId;
    public String                   messages[] = {"","","","",""};

    public int                      qos = 0;

    public FragmentManager          fragmentManager;
    public FragmentTransaction      fragmentTransaction;

    public MqttClient               client;
    public MqttConnectOptions       options;
    public MQTTSubscribeFragment    subscribeFragment;

    public Context                  context;

    public Uri                      pathUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.detail_layout);

        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       fragmentManager     = getSupportFragmentManager();

        // Check whether the activity is using the layout version with
        // the fragment_container FrameLayout. If so, we must add the first fragment
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            setTitle(getString(R.string.mqtt_connect));
            // Create an instance of ExampleFragment
            MQTTConnectFragment connectFragment = new MQTTConnectFragment();

            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            //connectFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, connectFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Interface to launch another fragment
    public void launchPublishFragment(String data) {

        setTitle(getString(R.string.mqtt_publish));

        MQTTPublishFragment publishFragment  = new MQTTPublishFragment();

        Bundle args = new Bundle();

        args.putString("topic",topicToPublish);
        args.putString("text", content);
        args.putInt   ("qos",qos);

        publishFragment.setArguments(args);

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, publishFragment);
        fragmentTransaction.commit();
    }

    // Interface to launch another fragment
    public void launchConnectFragment(String data) {

        setTitle(getString(R.string.mqtt_connect));

        MQTTConnectFragment connectFragment  = new MQTTConnectFragment();

        Bundle args = new Bundle();

        args.putString("broker", broker);
        args.putString("port",   port);
        args.putString("client", clientId);

        connectFragment.setArguments(args);

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, connectFragment);
        fragmentTransaction.commit();
    }

    // Interface to launch another fragment
    public void launchSubscribeFragment(String data) {

        setTitle(getString(R.string.mqtt_subscribe));

        subscribeFragment  = new MQTTSubscribeFragment();

        Bundle args = new Bundle();
        args.putString("topic", topicToSubscribe);
        args.putStringArray("messages", messages);

        subscribeFragment.setArguments(args);

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, subscribeFragment);
        fragmentTransaction.commit();
    }


    // Interface to call the construction of MQTT client from fragments.
    public void createMQTTClient(String connectParams[]){

        MQTTClientClass mqttClient = new MQTTClientClass();
        mqttClient.execute(connectParams);
    }

    // Interface to publish an MQTT message to topicToPublish.
    public void publishMQTTmessage(String publishParams[]) {

        MQTTClientClass mqttClient = new MQTTClientClass();
        mqttClient.execute(publishParams);
    }

    // Interface to subscribe the MQTT client to a given topicToPublish
    public void subscribeMQTTtopic(String subscribeParams[]){

        try {
            client.setCallback(this);
            client.subscribe(subscribeParams[1]);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Could not subscribe ", Toast.LENGTH_SHORT).show();

        }
        Toast.makeText(getApplicationContext(), "Subscribed to Topic " + subscribeParams[1], Toast.LENGTH_SHORT).show();
    }


    public void findFile(){
        Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
        fileintent.setType("*/*");

        try {
            startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
        } catch (ActivityNotFoundException e) {
            Log.e("tag", "No activity can handle picking a file. Showing alternatives.");
        }
    }


     @Override
     public  void onActivityResult(int requestCode, int resultCode, Intent data) {
         // TODO Fix no activity available
         if (data == null)
             return;
         switch (requestCode) {
             case PICKFILE_RESULT_CODE:
                 if (resultCode == RESULT_OK) {
                     pathUri = data.getData();
                 }
         }
     }


    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(String topic, MqttMessage message)
            throws Exception {

        MQTTmessage     = message.toString();
        topicToSubscribe = topic;

        for(int i = 4; i >= 1; i--){
            messages[i] =  messages[i-1];
        }
        messages[0] = topic + "/" + message;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    if(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof MQTTSubscribeFragment ) {
                        MQTTSubscribeFragment fragment_obj = (MQTTSubscribeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                        fragment_obj.updateList(messages);
                    }
                } catch (Exception e) {
                    Log.d("Error", "" + e);
                }
            }
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
    }

    //The AsyncTask is called with <Params, Progress, Result>
    public class MQTTClientClass extends AsyncTask<String, Void, String[]> {

        public String              brokerURI;

        @Override
        protected String[] doInBackground(String... paramString) {
            Log.d("Things Flow", "Started Async Task");

            /*
            TODO: The construction of the MQTTClient, and the URI are incomplete, need to check TCP address
            In the constructor "context" was substituted by "this"
            */
            switch (paramString[0]){

                case "connect":
                    broker                        = paramString[1];
                    port                          = paramString[2];
                    brokerURI                     = paramString[3];
                    clientId                      = paramString[4];

                    MemoryPersistence persistence = new MemoryPersistence();

                    try {
                        client  = new MqttClient(brokerURI, clientId, persistence);
                        options = new MqttConnectOptions();
                        options.setCleanSession(true);

                        options.setConnectionTimeout(60);
                        options.setKeepAliveInterval(60);

                        if(paramString[5] == "tcp"){

                        }else {
                            InputStream caCert = getContentResolver().openInputStream(pathUri);
                            InputStream clientCert  = getResources().openRawResource(R.raw.certificate);
                            InputStream privateKey  = getResources().openRawResource(R.raw.privatekey);
                            options.setSocketFactory(SslUtil.getSocketFactory("1234", paramString[1], paramString[2], caCert, clientCert, privateKey));
                        }

                        System.out.println("Connecting to broker: " + broker);
                        client.connect(options);
                        System.out.println("Connected");

                        return paramString;

                    } catch(MqttException me) {
                        System.out.println("reason "+me.getReasonCode());
                        System.out.println("msg "   +me.getMessage());
                        System.out.println("loc "   +me.getLocalizedMessage());
                        System.out.println("cause " +me.getCause());
                        System.out.println("excep " + me);
                        me.printStackTrace();
                    } catch (Exception e) {
                        Log.d("Things Flow I/O", "Error " + e);
                        e.printStackTrace();
                    }
                    break;

                case "publish":

                    content         = paramString[1];
                    topicToPublish  = paramString[2];
                    qos             = Integer.parseInt(paramString[3]);

                    System.out.println("Publishing message: " + content);
                    MqttMessage message = new MqttMessage(content.getBytes());
                    message.setQos(qos);

                    try {
                        client.publish(topicToPublish, message);

                        System.out.println("Message published");
                    } catch (MqttException e) {
                        e.printStackTrace();
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    return paramString;


                case "subscribe":

                    //

                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            if(result != null){
                switch (result[0]){
                    case "connect":
                        Log.d("Connect", "just connected");
                        Toast.makeText(getApplicationContext(), "Connected to " + broker + " on Port " + port, Toast.LENGTH_SHORT).show();
                        break;

                    case "publish":
                        Log.d("Publish", "just published");
                        Toast.makeText(getApplicationContext(), "Published " + content + " on Topic " + topicToPublish, Toast.LENGTH_SHORT).show();
                        break;

                    case "subscribe":
                        Toast.makeText(getApplicationContext(), "Subscribed " + content+ " to Topic " + topicToPublish, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            else{
                Toast.makeText(getApplicationContext(), "Could not perform action, check Connectivity ", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
