package com.example.android.beproject;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity implements MessageListener {


    private boolean mLocationPermissionGranted = false;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9001;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    TextView hintText;
    Button button;
    Button button2;
    ImageView homeImage;
    private static String TAG = "MainActivity: ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        homeImage = (ImageView) findViewById(R.id.home_image);
        hintText = (TextView) findViewById(R.id.hint_text);
        updateMainActivity();
        //Register sms listener
        smsBroadcastReceiver.bindListener(this);
        button = findViewById(R.id.map_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Toast.makeText(MainActivity.this, "Go to mapView", Toast.LENGTH_SHORT).show();
                goToMapView();
            }
        });
        button2 = findViewById(R.id.bt_button);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Toast.makeText(MainActivity.this, "BTView", Toast.LENGTH_SHORT).show();
                goToBTView();
            }
        });
        button.setEnabled(false);
        button2.setEnabled(false);

        // For checking the permissions of location and map
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                updateMainActivity();
                Toast.makeText(this, "You are Ready to Roll", Toast.LENGTH_SHORT).show();
                button.setEnabled(true);
                button2.setEnabled(true);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"Resuming Main");
        if(checkMapServices())  {
            if(mLocationPermissionGranted)  {
                updateMainActivity();
                button.setEnabled(true);
                button2.setEnabled(true);
            }
        }
    }

    public void updateMainActivity() {
        Log.i(TAG,"Updating MainActivity");
        if(!mLocationPermissionGranted) {
            homeImage.setImageResource(R.drawable.warning);
            hintText.setText(R.string.get_permission);
        } else if(BTActivity.clientActive)  {
            homeImage.setImageResource(R.drawable.checked);
            hintText.setText(R.string.ready);
        } else
        homeImage.setImageResource(R.drawable.bluetooth);
        hintText.setText(R.string.enable_bt);
    }

    private void goToBTView() {
        Intent btStart = new Intent(this,BTActivity.class);
        startActivity(btStart);
    }

    private void goToMapView() {

            Intent mapStart = new Intent(this,MapActivity.class);
            startActivity(mapStart);
    }
    // Functions for permissions

    private boolean checkMapServices(){
        if(isServicesOK()){
            return isMapsEnabled();
        }
        return false;
    }


    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available,8000);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        mLocationPermissionGranted = true;
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    Toast.makeText(this, "Go to BtView", Toast.LENGTH_SHORT).show();
                    button.setEnabled(true);
                    button2.setEnabled(true);
                    updateMainActivity();
                    goToBTView();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }


    //Preprocessing the received sms
    @Override
    public void messageReceived(String sender, String message) {
        Toast.makeText(this, "New Message Received: " +"\n" +sender +"\n" + message, Toast.LENGTH_SHORT).show();
        String sendable ="sms ";
        sendable += message;
        byte[] bytes =sendable.getBytes(Charset.defaultCharset());
        if(BTActivity.clientActive) {
            BTActivity.mBluetoothConnection.write(bytes);
        }
    }

}
