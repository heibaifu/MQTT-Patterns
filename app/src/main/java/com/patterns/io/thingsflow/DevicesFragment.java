package com.patterns.io.thingsflow;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class DevicesFragment extends Fragment {

    public DevicesFragment() {

    }

    @Override
    //@TargetApi(18) // Set this function to point at higher API to include the BLE calls.
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            //FetchDevicesTask devicesTask = new FetchDevicesTask();
            //devicesTask.execute("22194,mx");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Find the listView by its ID
        FloatingActionButton fabNext = (FloatingActionButton) rootView.findViewById(R.id.fabNext);

        fabNext.setOnClickListener(onClickListenerMQTT);

        return rootView;

    }


    private View.OnClickListener onClickListenerMQTT = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            Intent MQTTintent = new Intent(getActivity(), MQTTActivity.class);
            startActivity(MQTTintent);

        }
    };


}
