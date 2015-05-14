package com.marioandhika.android.wearable.undistract;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by marioandhika on 5/5/15.
 */
public class MainReceiver extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks {

    public static final String START_ACTIVITY_PATH = "/start-activity";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        SharedPreferences sp = context.getSharedPreferences("default", Context.MODE_PRIVATE);
        String configuredAddress = sp.getString("CONFIGURED_MAC", "");

        if (device.getAddress().equals(configuredAddress)) {

            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (final Node node : getConnectedNodesResult.getNodes()) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                    START_ACTIVITY_PATH, new byte[0]);
                        }
                    }
                });

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
