package com.davtyan.mytaxi.view.activitys;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.davtyan.mytaxi.R;

public class CallActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        findViewById(R.id.is_net).setOnClickListener(this);
        findViewById(R.id.call_phone).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.is_net:
                checkNetworkState();
                break;
            case R.id.call_phone:
                call();
                break;
        }
    }

    private void checkNetworkState() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            Intent intent = new Intent(CallActivity.this, RegisterActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(CallActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
        }
    }


    private void call() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:111"));
        if (ContextCompat.checkSelfPermission(CallActivity.this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        }
    }

}
