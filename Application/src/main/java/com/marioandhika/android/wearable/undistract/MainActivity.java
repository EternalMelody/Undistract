/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marioandhika.android.wearable.undistract;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Set;

/**
 * Has a single button, used to start the Wearable MainActivity.
 */
public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, AdapterView.OnItemClickListener {
    private static final int ENABLE_BLUETOOTH_CODE = 1;
    private BluetoothAdapter adapter;

    private GoogleApiClient mGoogleApiClient;
    private ListView mListView;
    private ArrayAdapter<DeviceEntry> mListAdapter;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.main_activity);

        // get bluetooth adapter
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();

        // check bluetooth status
        if (adapter.isEnabled()){
            showDevices();
        } else {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, ENABLE_BLUETOOTH_CODE);
        }

        // TODO load sharedpreference for currently set-up device
        SharedPreferences sp = getSharedPreferences("default", Context.MODE_PRIVATE);
        String name = sp.getString("CONFIGURED_NAME", "No device configured yet");
        String mac = sp.getString("CONFIGURED_MAC", "");
        // TODO populate view with currently set up device
        updateConfiguredDevice(name);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                        // .addConnectionCallbacks(this)
                        // .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check result code
        if (resultCode == Activity.RESULT_OK) {
            showDevices();
        } else {
            Toast.makeText(this, "You need to enable bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showDevices() {
        // poll for paired bluetooth devices
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        ArrayList<DeviceEntry> myDataset = new ArrayList<>();

        // TODO populate recyclerview with paired bluetooth devices
        for (BluetoothDevice device: devices){
            myDataset.add(new DeviceEntry(device.getName(), device.getAddress()));
        }


        mListView = (ListView) findViewById(R.id.list_view);

        mListAdapter = new DevicesListAdapter(this, myDataset);

        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(this);
    }

    private void updateConfiguredDevice(String string){
        View view = findViewById(R.id.configured_device_view);
        TextView configuredTextView = (TextView) view.findViewById(R.id.device_text_view);
        configuredTextView.setText(string);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Button button = (Button) findViewById(R.id.button);
        button.setEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Button button = (Button) findViewById(R.id.button);
        button.setEnabled(false);
    }

    public void onForceActivate(View v){
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (final Node node : getConnectedNodesResult.getNodes()) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                    MainReceiver.START_ACTIVITY_PATH, new byte[0]);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences sp = getSharedPreferences("default", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        DeviceEntry deviceEntry = mListAdapter.getItem(position);
        editor.putString("CONFIGURED_NAME", deviceEntry.getName());
        editor.putString("CONFIGURED_MAC", deviceEntry.getMac());
        editor.apply();
        Toast.makeText(this,"Configured device changed to: " + deviceEntry.getName(),Toast.LENGTH_SHORT).show();
        updateConfiguredDevice(deviceEntry.getName());
    }

    public class DevicesListAdapter extends ArrayAdapter<DeviceEntry> {

        public DevicesListAdapter(Context context, ArrayList<DeviceEntry> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeviceEntry deviceEntry = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_view, parent, false);
            }

            TextView name = (TextView) convertView.findViewById(R.id.device_text_view);
            name.setText(deviceEntry.getName());

            return convertView;
        }
    }
}

