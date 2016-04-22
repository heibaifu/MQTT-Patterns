package com.patterns.io.thingsflow;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class ConnectFragment extends Fragment {

    public int simon;

    public EditText             textBroker;
    public EditText             textPort;

    public FloatingActionButton fabConnect;
    public FloatingActionButton fabPublish;

    public FragmentManager      fragmentManager;
    public FragmentTransaction  fragmentTransaction;

    public DataPassListener     mCallback;

    public ConnectFragment() {
        // Required empty public constructor
    }

    public interface DataPassListener{
        public void launchPublishFragment(String data);
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
                mCallback = (DataPassListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnHeadlineSelectedListener");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_connect, container, false);

        fragmentManager     = getFragmentManager();

        fabConnect          = (FloatingActionButton) rootView.findViewById(R.id.fabConnect);
        fabPublish          = (FloatingActionButton) rootView.findViewById(R.id.fabPublish);

        fabConnect.setOnClickListener(onClickListenerMQTT);
        fabPublish.setOnClickListener(onClickListenerMQTT);

        return rootView;
    }

    private View.OnClickListener onClickListenerMQTT = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.fabPublish:
                    //Handle Button click
                    mCallback.launchPublishFragment("Text to pass FragmentB");
                    break;

                case R.id.fabConnect:
                    //Handle Button click
                    break;
            }
        }
    };


}
