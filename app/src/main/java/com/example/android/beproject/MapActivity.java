package com.example.android.beproject;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final String TAG = "MapActivity";
    private Place mSelectedPlace ;
    public static String mUrl;
    private static String urlResponse;
    private FusedLocationProviderClient mFusedLocationClient;
    public static Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Places.initialize(getApplicationContext(),getString(R.string.google_maps_api_key));
        PlacesClient placesClient = Places.createClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastKnownLocation();
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
        if(!mMap.isTrafficEnabled())    {
            mMap.setTrafficEnabled(true);
        }
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        // Specify the types of place data to return.
        try {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        }
        catch (Exception e) {
            Log.e(TAG,"error");
        }
        autocompleteFragment.setCountry("IN");

        //Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                mSelectedPlace = place ;
                setMarker(mSelectedPlace);
                mUrl = getApiUrl(mSelectedPlace.getId());
                DirectionAsyncTask task = new DirectionAsyncTask();
                task.execute();
                DirectionJ directionJ = new DirectionJ(urlResponse);
                if (directionJ.getStatus().equals("OK"))    {
                    Log.i(TAG,"Directions recieved");
                }
                else    {
                    Log.e(TAG,"Could not retrieve directions because:" + directionJ.getStatus());

                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private String getApiUrl(String id) {
        String url ="https://maps.googleapis.com/maps/api/directions/json?origin=";
        url += mLocation.getLatitude() + "," + mLocation.getLongitude() ;
        url += "&destination=place_id:" + id;
        url += "&key=" + getString(R.string.google_maps_api_key);
        return url;

    }

    private void setMarker(Place place)    {
        // Add a marker in Sydney and move the camera
        if (place != null) {
            LatLng latlng = place.getLatLng();
            mMap.addMarker(new MarkerOptions().position(latlng).title(place.getName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15));
        }
    }

    public void getLastKnownLocation() {
        Log.d(TAG,"lastLnownLocationCalled. ");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location != null) {
                        Log.d(TAG,"onComplete: latitude" + location.getLatitude());
                        Log.d(TAG,"onComplete: longitude" + location.getLongitude());
                        mLocation=location;
                    }
                    else
                        Log.d(TAG,"location = Null");

                }
            }
        });
    }
    public static void reply(String string)    {
        urlResponse = string;
    }
}
