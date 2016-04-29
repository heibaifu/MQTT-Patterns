package com.patterns.io.thingsflow;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class MQTTConnectFragment extends Fragment {

    private static final int PICKFILE_RESULT_CODE = 1;

    public String brokerString;
    public String portString;
    public String clientString;
    public String protocol;
    public String filePath;

    public boolean TLS = false;

    public EditText textBroker;
    public EditText textPort;
    public EditText textClient;

    public FloatingActionButton fabConnectToBroker;
    public FloatingActionButton fabPublish;
    public FloatingActionButton fabSubscribe;

    public RadioButton radioNormal;
    public RadioButton radioTLS;

    public FragmentManager fragmentManager;

    public ConnectDataPassListener mCallback;

    public MQTTConnectFragment() {
        // Required empty public constructor
    }

    public interface ConnectDataPassListener {
        void launchPublishFragment(String data);

        void launchSubscribeFragment(String data);

        void createMQTTClient(String connectParams[]);

        void findFile();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;

        if (context instanceof Activity) {
            activity = (Activity) context;

            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mCallback = (ConnectDataPassListener) activity;

            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement ConnectDataPassListener");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //inflater.inflate(R.menu.devicefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d("get item", "" + id);
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Log.d("get item", "" + id);

            final Dialog dialog = new Dialog(getActivity());

            dialog.setContentView(R.layout.about_layout);
            dialog.setTitle("About Things Flow");

            Button btnCancel = (Button) dialog.findViewById(R.id.dismiss);
            dialog.show();

            btnCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_connect, container, false);

        fragmentManager = getFragmentManager();


        fabConnectToBroker = (FloatingActionButton) rootView.findViewById(R.id.fabConnectToBroker);
        fabPublish = (FloatingActionButton) rootView.findViewById(R.id.fabPublish);
        fabSubscribe = (FloatingActionButton) rootView.findViewById(R.id.fabSubscribe);

        textBroker = (EditText) rootView.findViewById(R.id.editTextBroker);
        textPort = (EditText) rootView.findViewById(R.id.editTextPort);
        textClient = (EditText) rootView.findViewById(R.id.editTextClient);

        radioNormal = (RadioButton) rootView.findViewById(R.id.radioNormal);
        radioTLS = (RadioButton) rootView.findViewById(R.id.radioTLS);

        fabConnectToBroker.setOnClickListener(onClickListenerMQTT);
        fabPublish.setOnClickListener(onClickListenerMQTT);
        fabSubscribe.setOnClickListener(onClickListenerMQTT);
        radioNormal.setOnClickListener(onClickListenerMQTT);
        radioTLS.setOnClickListener(onClickListenerMQTT);

        textBroker.setText("192.168.0.11");
        textPort.setText("1883");
        textClient.setText("SampleClient");

        radioNormal.setChecked(true);

        return rootView;
    }

    @Override
    public void onStart() {

        super.onStart();
        Bundle args = getArguments();
        if (args != null) {
            //TODO: add the data to be passed to this fragment
            textBroker.setText(args.getString("broker"));
            textPort.setText(args.getString("port"));
            textClient.setText(args.getString("client"));

            if (TLS) {
                radioNormal.setChecked(false);
                radioTLS.setChecked(true);

            } else {
                radioNormal.setChecked(true);
                radioTLS.setChecked(false);
            }
        }
    }

    private View.OnClickListener onClickListenerMQTT = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.fabConnectToBroker:
                    //Handle Button click

                    brokerString = textBroker.getText().toString();
                    portString = textPort.getText().toString();
                    clientString = textClient.getText().toString();

                    String URIbroker;

                    if (!TLS) {
                        URIbroker = "tcp://" + brokerString + ":" + portString;
                        protocol = "tcp";
                    } else {
                        URIbroker = "ssl://" + brokerString + ":" + portString;
                        protocol = "ssl";
                    }

                    String connectParams[] = {"connect", brokerString, portString, URIbroker, clientString, protocol, filePath};
                    mCallback.createMQTTClient(connectParams);

                    break;
                case R.id.fabPublish:
                    //Handle Button click
                    mCallback.launchPublishFragment("");
                    break;

                case R.id.fabSubscribe:
                    //Handle Button click
                    mCallback.launchSubscribeFragment("");
                    break;

                case R.id.radioNormal:
                    radioTLS.setChecked(false);
                    TLS = false;
                    break;

                case R.id.radioTLS:
                    radioNormal.setChecked(false);
                    textPort.setText("8883");
                    TLS = true;

                    mCallback.findFile();

                    break;
            }
        }
    };

}



