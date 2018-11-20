package com.ti4e.wira.cbnavbar;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LastKnown extends AppCompatActivity {
    Button mLocationButton,mPlacePickerButton;
    private Location mLastLocation;
    TextView mLocationTextView;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationButton = (Button) findViewById(R.id.button_location);
        mLocationTextView = (TextView) findViewById(R.id.textview_location);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mPlacePickerButton = (Button) findViewById(R.id.button_placepicker);
        mPlacePickerButton.setVisibility(View.GONE);

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    mulaiTrackingLokasi();


            }
        });

    }

    private void mulaiTrackingLokasi() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION
            );
        }else {

            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null){
                        mLastLocation = location;
                        mLocationTextView.setText(getString(R.string.location_text,
                                mLastLocation.getLatitude(),
                                mLastLocation.getLongitude(),
                                mLastLocation.getTime()));
                    }else {
                        mLocationTextView.setText("lokasi tidak tersedia");
                    }
                }
            });

        }
    }
}
