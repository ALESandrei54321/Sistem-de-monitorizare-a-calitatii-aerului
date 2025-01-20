package com.bluealert.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            Toast.makeText(context, "Connected to the internet", Toast.LENGTH_SHORT).show();
            // Perform actions when connected to the internet
        } else {
            Toast.makeText(context, "Disconnected from the internet", Toast.LENGTH_SHORT).show();
            // Perform actions when disconnected from the internet
        }
    }
}