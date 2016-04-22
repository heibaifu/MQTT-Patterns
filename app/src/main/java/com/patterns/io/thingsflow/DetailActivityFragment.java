package com.patterns.io.thingsflow;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.InputStream;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    final static String puerto = "";

    public EditText             textBroker;
    public EditText             textPort;
    public EditText             textPublish;
    public EditText             textPublishTopic;

    public FloatingActionButton fab;
    public FloatingActionButton fabConnect;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.



        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.devicefragment, menu);
        //getMenuInflater().inflate(R.menu.devicefragment, menu);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        //View rootView = inflater.inflate(R.layout.detail_layout, container, false);

        ///////////////  ALL THIS IS   I/O    MQTT STUFF //////////////////////////////////////////
        // Find the listView by its ID
        fab                 = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fabConnect          = (FloatingActionButton) rootView.findViewById(R.id.fabConnect);

        textBroker          = (EditText)             rootView.findViewById(R.id.editTextBroker);
        textPort            = (EditText)             rootView.findViewById(R.id.editTextPort);
        textPublish         = (EditText)             rootView.findViewById(R.id.editTextPublish);
        textPublishTopic    = (EditText)             rootView.findViewById(R.id.editTextPublishTopic);

        fab       .setOnClickListener(onClickListenerMQTT);
        fabConnect.setOnClickListener(onClickListenerMQTT);

        //textBroker.setText("192.168.0.11");
        //textBroker.setText("A33DKVX6YQAT9A.iot.us-west-2.amazonaws.com");

        return rootView;
    }

    @Override
    public void onStart(){

        super.onStart();
        Bundle args = getArguments();
        if (args != null) {
            textBroker.setText(args.getString(puerto));
        }

    }


    private OnClickListener onClickListenerMQTT = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.fab:
                    //Handle Button click
                    SendMQTT mqttSender = new SendMQTT();

                    String stringBroker = textBroker        .getText().toString();
                    String stringPort   = textPort          .getText().toString();
                    String textToPublish= textPublish       .getText().toString();
                    String publishTopic = textPublishTopic  .getText().toString();

                    String URIbroker    = "ssl://"+stringBroker+":"+stringPort;

                    String argument[] = {stringBroker,stringPort, URIbroker,publishTopic,textToPublish};
                    mqttSender.execute(argument);
                    break;

                case R.id.fabConnect:
                    //Handle Button click
                    ConnectFragment connectFragment = new ConnectFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, connectFragment);
                    fragmentTransaction.commit();
                    break;
            }
        }
    };




    //The AsyncTask is called with <Params, Progress, Result>
    public class SendMQTT extends AsyncTask<String, Void, String[]> {

        public MqttAndroidClient    client;
        public Context              context;
        public MqttMessage          message;
        public String               content     = "Mensaje Tiburon";
        public String               topic       = "MQTT Examples";

        @Override
        protected String[] doInBackground(String... paramString) {
            Log.d("Things Flow", "Started Async Task");

            /*
            TODO: The construction of the MQTTClient, and the URI are incomplete, need to check TCP address
            In the constructor "context" was substituted by "this"
            */

            String connectionURI = paramString[2];

            String topic                = paramString[3];
            String content              = paramString[4];
            int qos                     = 0;
            String broker               = connectionURI;
            String clientId             = "RobertoClienteDeMQTT";
            MemoryPersistence persistence = new MemoryPersistence();

            try {
                MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);

                ///////////////////////////////////////////////////////////////////////////////////
                options.setConnectionTimeout(60);
                options.setKeepAliveInterval(60);

                InputStream caCert      = getActivity().getResources().openRawResource(R.raw.root);
                InputStream clientCert  = getActivity().getResources().openRawResource(R.raw.certificate);
                InputStream privateKey  = getActivity().getResources().openRawResource(R.raw.privatekey);

                Log.d("Things Flow - I/O", "Checkpoint 1");
                options.setSocketFactory(SslUtil.getSocketFactory("1234", paramString[0], paramString[1], caCert, clientCert, privateKey));
                //////////////////////////////////////////////////////////////////////////////////
                Log.d("Things Flow - I/O", "Checkpoint 2");
                System.out.println("Connecting to broker: " + broker);
                sampleClient.connect(options);
                System.out.println("Connected");
                System.out.println("Publishing message: " + content);
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(qos);
                sampleClient.publish(topic, message);
                System.out.println("Message published");
                //sampleClient.disconnect();
                //System.out.println("Disconnected");
                //System.exit(0);
            } catch(MqttException me) {
                System.out.println("reason "+me.getReasonCode());
                System.out.println("msg "+me.getMessage());
                System.out.println("loc "+me.getLocalizedMessage());
                System.out.println("cause "+me.getCause());
                System.out.println("excep "+me);
                me.printStackTrace();
            } catch (Exception e) {
                Log.d("Things Flow I/O", "Error " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            if(result != null){
                Toast.makeText(getActivity(), "Successfully published to topic " + topic, Toast.LENGTH_SHORT).show();

            }
        }
    }
}
