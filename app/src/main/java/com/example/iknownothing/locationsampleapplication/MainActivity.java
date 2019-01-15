package com.example.iknownothing.locationsampleapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MainActivity extends AppCompatActivity implements LocationListener {

    final static int REQUEST_LOCATION = 199;
    public static final String MyPREFERENCES = "MyPrefs";
    static int count = 0;

    static String locationAddress;

    static public SharedPreferences sharedPreferences;
    private GoogleApiClient googleApiClient;

    LocationManager locationManager;
    Location location;
    boolean isgpsEnabled, isnetworkEnabled;
    static double latitude = 0.0, longitude = 0.0;
    private TextView latitudeText, longitudeText, lastknownLocation, sharedpreferencelatlon;
    private static TextView tvAddress;
    private SwipeRefreshLayout swipeRefresh;
    ProgressDialog progressDialog;
    Button allow_me;

    //LocationProvider locationProvider;
//LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setFinishOnTouchOutside(true);


        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        sharedpreferencelatlon = findViewById(R.id.sharedpreferencelatlon);

        allow_me = findViewById(R.id.allow_me);
        allow_me.setVisibility(View.GONE);

        allow_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    enableLoc();
                }
                else{
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);

                }

            }
        });
    /*SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putFloat("last_latitude",(float) 0.0);
    editor.putFloat("last_longitude",(float) 0.0);
    editor.apply();
*/


        Log.d("this", "last_latitude - " + sharedPreferences.getFloat("last_latitude", 0));
        Log.d("this", "last_longitude - " + sharedPreferences.getFloat("last_longitude", 0));
        Log.d("this", "last_city - " + sharedPreferences.getString("last_city", ""));

        sharedpreferencelatlon.setText("Latitude - " + sharedPreferences.getFloat("last_latitude", 0) +
                "\n" + "Longitude - " + sharedPreferences.getFloat("last_longitude", 0));
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Waiting for location");


        //loader......


        //Finding the coordinates....
        locationsearch();

        checkSharedPreferences();


        //Whenever the user swipe down the activity,the we again search the coordinates....

        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                locationsearch();
                swipeRefresh.setRefreshing(false);
            }
        });


    }


    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        latitudeText.setText("\n\nonLocationChanged Latitude = " + latitude);
        longitudeText.setText("onLocationChanged Longitude = " + longitude);


        //setting the latitude and longitude of shared preferences from latitude and longitude global variables...
        setCoordinates();

        //UI Upate
        //Checking if the lastknown location is 100km far from the location got from onLocationChanged Method....
        if (progressDialog != null && progressDialog.isShowing()) {

            checkSharedPreferences();
            progressDialog.dismiss();

        } else {




        }



/*
    int distance =(int) getDistanceFromLatLonInKm(latitude,longitude,
            (double) sharedPreferences.getFloat("last_latitude",0),(double)
                    sharedPreferences.getFloat("last_longitude",0));

Log.d("this","Distance is "+distance+" kms");
    if(distance>100)
    {
    LocationAddress locationAddress = new LocationAddress();
    locationAddress.getAddressFromLocation(latitude, longitude,
            getApplicationContext(), new GeocoderHandler());

    Log.d("this","Distance is more than 100 kms");



    }

*/

        //count = count+1;
        //Log.d("count",count+"");

        Log.d("this", "lat " + String.valueOf(location.getLatitude()) + "   long " + String.valueOf(location.getLongitude()));
        Toast.makeText(this, "This is OnLocationChanged Method Result" + "\n" + "lat" + latitude + "long" + longitude, Toast.LENGTH_SHORT).show();

        //Only once it will run the onLocationChanged() method...
        locationManager.removeUpdates(this);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    protected void onStop() {
        super.onStop();


    }


    protected void locationsearch() {


        latitudeText = findViewById(R.id.latitude);
        longitudeText = findViewById(R.id.longitude);
        lastknownLocation = findViewById(R.id.lastknownlocation);
        tvAddress = findViewById(R.id.tvAddress);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


            isgpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isnetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.d("this", "GPS Enabled is " + String.valueOf(isgpsEnabled));

            Log.d("this", "Network Enabled is = " + String.valueOf(isnetworkEnabled));


            if (!isnetworkEnabled && !isgpsEnabled) {

                enableLoc();
                Log.d("this", "CAN'T FIND LOCATION");

            } else {

                if (!isnetworkEnabled) {
                    enableLoc();
                }

                if (!isgpsEnabled) {
                    enableLoc();
                }


                if (isnetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            0,
                            0,
                            this);
                    Log.d("check", "running");


                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        Log.d("this", String.valueOf(location));
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();


                            //Setting lat and lang in shared preferences....
                            setCoordinates();


                            lastknownLocation.setText("Network : Latitude = " + latitude + "\n"
                                    + "Network : Longitude = " + longitude);
                            Log.d("this", "Welcome");
                            Log.d("this", "network " + String.valueOf(latitude));
                            Log.d("this", "network " + String.valueOf(longitude));

                        }
                    }
                }

                if (isgpsEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            0,
                            0,
                            this
                    );

                    Log.d("check", "running");

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if (location != null) {

                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            //Setting lat and lang in shared preferences....
                            setCoordinates();


                            lastknownLocation.setText("GPS : Latitude = " + latitude + "\n"
                                    + "GPS : longitude = " + longitude);

                            Log.d("this", "gps " + String.valueOf(latitude));
                            Log.d("this", "gps " + String.valueOf(longitude));
                        }
                    }

                }
            }


        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private static class GeocoderHandler extends Handler {
        @Override

        public void handleMessage(Message message) {


            switch (message.what) {
                case 1:
                    Bundle bundle1 = message.getData();
                    locationAddress = bundle1.getString("address");
                    tvAddress.setText("You are in " + locationAddress + "\n");

                    setLastCoordinates();

                    break;
                case 2:
                    Bundle bundle2 = message.getData();
                    locationAddress = bundle2.getString("address");
                    tvAddress.setText("You are in " + locationAddress + "\n");

                    setLastCoordinates();

                default:
                    locationAddress = null;
                    tvAddress.setText("This city is unavaliable");

                    setLastCoordinates();
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case 100: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Finding the coordinates....
                    allow_me.setVisibility(View.GONE);
                    progressDialog.show();
                    locationsearch();

                    sharedpreferencelatlon.setText("Latitude - " + sharedPreferences.getFloat("last_latitude", 0) +
                            "\n" + "Longitude - " + sharedPreferences.getFloat("last_longitude", 0));


                   /* LocationAddress locationAddress = new LocationAddress();
                    locationAddress.getAddressFromLocation(latitude, longitude,
                            getApplicationContext(), new GeocoderHandler());

                    Log.d("this","onCreate LastKnownLocation Distance is more than 100 kms");
                    */


                } else {
                    Toast.makeText(this, "No Permissions", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    allow_me.setVisibility(View.VISIBLE);
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //HERE WE CALLING GEOCODER CLASS WHEN > 100KMS, IF NOT THEN GETTING CITY FROM SHARED PREFERENCES.....
    public void checkSharedPreferences() {

        if (sharedPreferences.getFloat("latitude", 0) != 0 && sharedPreferences.getFloat("longitude", 0) != 0) {

            if (getDistanceFromLatLonInKm(sharedPreferences.getFloat("latitude", 0),
                    sharedPreferences.getFloat("longitude", 0),
                    sharedPreferences.getFloat("last_latitude", 0),
                    sharedPreferences.getFloat("last_longitude", 0)) > 100) {
                LocationAddress locationAddress = new LocationAddress();
                locationAddress.getAddressFromLocation(latitude, longitude,
                        getApplicationContext(), new GeocoderHandler());

                Log.d("this", "onCreate LastKnownLocation Distance is more than 100 kms");

            } else {
                tvAddress = findViewById(R.id.tvAddress);
                tvAddress.setText("You are in " + sharedPreferences.getString("last_city", "-"));
            }

            progressDialog.dismiss();
        } else {
            //new progress dialog...

            progressDialog.show();

        }
    }

    //SETTING LAST_LAT AND LAST_LANG of Shared Preferences WHEN GEOCODER CALL HAS BEEN MADE....
    public static void setLastCoordinates() {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("last_latitude", (float) latitude);
        editor.putFloat("last_longitude", (float) longitude);
        editor.putString("last_city", locationAddress);
        editor.apply();

        Log.d("this", locationAddress);
    }

    //setting the latitude and longitude of shared preferences from latitude and longitude global variables...
    public void setCoordinates() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat("latitude", (float) latitude);
        editor.putFloat("longitude", (float) longitude);
        editor.apply();
    }

    //GETTING DISTANCE BY USING HAVERSINE FORMULAE....
    public double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        int R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2 - lat1);  // deg2rad below
        double dLon = deg2rad(lon2 - lon1);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c; // Distance in km
        return d;
    }

    public double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }

    //IF GPS HAS BEEN TURNED OFF THEN WE CAN SHOW PROMPT THAT ASK USER TO SWITCH ON THE GPS....
    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(5000);
            //locationRequest.setSmallestDisplacement(100*1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);


            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);


                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        } else {
            googleApiClient = null;

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION) {
            if (resultCode == MainActivity.RESULT_OK) {

                locationsearch();

            }
            if (resultCode == MainActivity.RESULT_CANCELED) {

                Toast.makeText(this, "Location is OFF", Toast.LENGTH_SHORT).show();

              //  progressDialog.dismiss();

                if (!isgpsEnabled && !isnetworkEnabled) {


                    progressDialog.dismiss();
                    allow_me.setVisibility(View.VISIBLE);

                }


                if (latitude == 0 && longitude == 0) {
                    Toast.makeText(this, "onActivityResult", Toast.LENGTH_SHORT).show();
                    locationsearch();

                }
                //Write your code if there's no result
            }
        }
    }


}
