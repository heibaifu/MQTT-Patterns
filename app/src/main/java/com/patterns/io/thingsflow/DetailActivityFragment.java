package com.patterns.io.thingsflow;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    //public SendMQTT             mqttSender;


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

        ///////////////  ALL THIS IS   I/O    MQTT STUFF //////////////////////////////////////////
        // Find the listView by its ID
        Button buttonMQTT = (Button) rootView.findViewById(R.id.buttonMQTT);

        buttonMQTT.setOnClickListener(onClickListenerMQTT);



        //return inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;
    }


    private OnClickListener onClickListenerMQTT = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.buttonMQTT:
                    //Handle Button click
                    SendMQTT mqttSender = new SendMQTT();
                    mqttSender.execute("simon,77");
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

            String connectionURI = "tcp://A3IDCGI3XQ4KE9.iot.us-west-2.amazonaws.com:8883";
            //String connectionURI = "tcp://broker.mqttdashboard.com:1883";
            String topic        = "MQTT/SHARK";
            String content      = "TIBURON TIBURON TIBURON TIBURON";
            int qos             = 1;
            String broker       = connectionURI;
            String clientId     = "JavaSample";
            MemoryPersistence persistence = new MemoryPersistence();

            try {
                MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                System.out.println("Connecting to broker: " + broker);
                sampleClient.connect(connOpts);
                System.out.println("Connected");
                System.out.println("Publishing message: " + content);
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(qos);
                sampleClient.publish(topic, message);
                System.out.println("Message published");
                sampleClient.disconnect();
                System.out.println("Disconnected");
                //System.exit(0);
            } catch(MqttException me) {
                System.out.println("reason "+me.getReasonCode());
                System.out.println("msg "+me.getMessage());
                System.out.println("loc "+me.getLocalizedMessage());
                System.out.println("cause "+me.getCause());
                System.out.println("excep "+me);
                me.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            if(result != null){


            }


        }
    }

}
