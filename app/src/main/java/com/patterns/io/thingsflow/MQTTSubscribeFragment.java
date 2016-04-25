package com.patterns.io.thingsflow;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MQTTSubscribeFragment extends Fragment {

    public String                       topicSubscribeString;
    public String                       messages[] = {"","","","",""};

    private ArrayAdapter<String>        messagesAdapter;

    public EditText                     topicSubscribe;

    public FloatingActionButton         fabSubscribeTotopic;
    public FloatingActionButton         fabConnect;
    public FloatingActionButton         fabLaunchPublish;

    public ListView                     listViewMessages;

    public SubscribeDataPassListener    mCallback;

    public MQTTSubscribeFragment() {}

    public interface SubscribeDataPassListener {
        void launchPublishFragment(String data);
        void launchConnectFragment(String data);
        void subscribeMQTTtopic(String messageParams[]);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;

        if (context instanceof Activity){
            activity=(Activity) context;

            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mCallback = (SubscribeDataPassListener) activity;

            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement SubscribeDataPassListener");
            }
        }
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_mqttsubscribe, container, false);

        ///////////////  ALL THIS IS   I/O    MQTT STUFF //////////////////////////////////////////
        // Find the listView by its ID
        fabSubscribeTotopic = (FloatingActionButton) rootView.findViewById(R.id.fabSubscribeToTopic);
        fabConnect          = (FloatingActionButton) rootView.findViewById(R.id.fabConnect);
        fabLaunchPublish    = (FloatingActionButton) rootView.findViewById(R.id.fabPublish);

        topicSubscribe      = (EditText)             rootView.findViewById(R.id.editTextSubscribeTopic);

        fabSubscribeTotopic .setOnClickListener(onClickListenerMQTT);
        fabConnect          .setOnClickListener(onClickListenerMQTT);
        fabLaunchPublish    .setOnClickListener(onClickListenerMQTT);

        List<String> MQTTmessages = new ArrayList<String>(Arrays.asList(messages));

        messagesAdapter = new ArrayAdapter<String>(
                // The current context (this fragment's parent activity)
                getActivity(),
                // ID of list item layout xml
                R.layout.list_item,
                // ID of textView to populate
                R.id.device_item,
                // Data to populate with
                MQTTmessages);

        // Find the listView by its ID
        listViewMessages = (ListView) rootView.findViewById(R.id.listViewMessages);

        // Bind the adapter to the List View
        listViewMessages.setAdapter(messagesAdapter);

        return rootView;
    }

    @Override
    public void onStart(){

        super.onStart();
        Bundle args = getArguments();
        if (args != null) {
            //TODO: add the data to be passed to this fragment
            //textBroker.setText(args.getString(puerto));
            topicSubscribe.setText(args.getString("topic"));

            messages = args.getStringArray("messages");
            if (messages != null) {
                messagesAdapter.clear();
                messagesAdapter.addAll(messages);
                // Bind the adapter to the List View
                listViewMessages.setAdapter(messagesAdapter);
            }

        }
    }

    private View.OnClickListener onClickListenerMQTT = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.fabSubscribeToTopic:
                    //Handle Button click
                    String topicToSubscribe= topicSubscribe       .getText().toString();
                    String connectParams[] = {"subscribe", topicToSubscribe};
                    mCallback.subscribeMQTTtopic(connectParams);
                    break;

                case R.id.fabPublish:
                    //Handle Button click
                    mCallback.launchPublishFragment("Text to pass FragmentB");
                    break;

                case R.id.fabConnect:
                    //Handle Button click
                    mCallback.launchConnectFragment("Text to pass FragmentB");
                    break;
            }
        }
    };

    public void updateList(String topic, String message){

        for(int i = 4; i >= 1; i--){
            Log.d("message Adapter", "in here" + i);
            messages[i] =  messages[i-1];
        }
        messages[0] = topic + "/" + message;

        messagesAdapter.clear();
        messagesAdapter.addAll(messages);

        // Bind the adapter to the List View
        listViewMessages.setAdapter(messagesAdapter);



    }
}
