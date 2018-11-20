package com.ti4e.wira.cbnavbar;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements DapatkanAlamatTask.onTaskSelesai{
    Button mLocationButton,mPlacePickerButton;
    private Location mLastLocation;
    TextView mLocationTextView;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION =1;
    private ImageView mAndroidImageView;
    private AnimatorSet mRotateAnim;

    private boolean mTrackingLocation;
    private LocationCallback mLocationCalback;

    @Override
    public void onTaskCOmpleted(String result) {
        if(mTrackingLocation){
            mLocationTextView.setText(getString(R.string.alamat_text,result,System.currentTimeMillis()));
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationButton = (Button) findViewById(R.id.button_location);
        mLocationTextView = (TextView) findViewById(R.id.textview_location);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mAndroidImageView = (ImageView) findViewById(R.id.imageView) ;
        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);
        mPlacePickerButton = (Button) findViewById(R.id.button_placepicker);
        mPlacePickerButton.setVisibility(View.GONE);

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mTrackingLocation){
                    mulaiTrackingLokasi();
                }else {
                    stopTrackingLokasi();
                }

            }
        });

        mLocationCalback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mTrackingLocation){
                    new DapatkanAlamatTask(MainActivity.this,MainActivity.this).execute(locationResult.getLastLocation());
                }
            }
        };
    }

    private void mulaiTrackingLokasi(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION
            );
        }else {
//            Log.d("getpermisi","getlocation:permission granted");
            mFusedLocationClient.requestLocationUpdates(getLocationRequest(),mLocationCalback,null);
//            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
//                @Override
//                public void onSuccess(Location location) {
//                    if (location != null){
////                        mLastLocation = location;
////                        mLocationTextView.setText(getString(R.string.location_text,
////                                mLastLocation.getLatitude(),
////                                mLastLocation.getLongitude(),
////                                mLastLocation.getTime()));
//
//                        new DapatkanAlamatTask(MainActivity.this,MainActivity.this).execute(location);
//                    }else {
//                        mLocationTextView.setText("lokasi tidak tersedia");
//                    }
//                }
//            });
            mLocationTextView.setText(getString(R.string.alamat_text,"sedang mencari alamat",System.currentTimeMillis()));
            mTrackingLocation = true;
            mLocationButton.setText("Stop Tracking LOkasi");
            mRotateAnim.start();
        }

    }
private void stopTrackingLokasi(){
        if(mTrackingLocation){
            mTrackingLocation = false;
            mFusedLocationClient.removeLocationUpdates(mLocationCalback);
            mLocationButton.setText("Mulai Tracking Lokasi");
            mLocationTextView.setText("Tracking sedang dihentikan");
            mRotateAnim.end();
        }

}
private LocationRequest getLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;

}
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case  REQUEST_LOCATION_PERMISSION:

                if (grantResults.length> 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    mulaiTrackingLokasi();
                }else {
                    Toast.makeText(this,"permission bapaknya gak bisa",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
