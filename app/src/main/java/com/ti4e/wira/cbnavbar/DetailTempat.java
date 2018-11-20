package com.ti4e.wira.cbnavbar;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DetailTempat extends AppCompatActivity implements DapatkanAlamatTask.onTaskSelesai{
    Button mLocationButton,mPlacePickerButton;
    private Location mLastLocation;
    TextView mLocationTextView;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION =1;
    private ImageView mAndroidImageView;
    private AnimatorSet mRotateAnim;

    private PlaceDetectionClient mPlaceDetectionClient;
    private String mLastPlaceName;

    private boolean mTrackingLocation;
    private LocationCallback mLocationCalback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

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
//                    getAddress(-7.946465,112.615548);
                }else {
                    stopTrackingLokasi();
                }

            }
        });

        mLocationCalback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mTrackingLocation){
                    new DapatkanAlamatTask(DetailTempat.this,DetailTempat.this).execute(locationResult.getLastLocation());
                }
            }
        };
    }

    @Override
    public void onTaskCOmpleted(final String result) throws  SecurityException{
        if(mTrackingLocation){
            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null );
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    if(task.isSuccessful()){
                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                        float maxLikelihood = 0;
                        Place currentPlace = null;
                        for (PlaceLikelihood placeLikelihood :likelyPlaces){
                            if(maxLikelihood < placeLikelihood.getLikelihood()){
                                maxLikelihood = placeLikelihood.getLikelihood();
                                currentPlace = placeLikelihood.getPlace();
                            }
                        }
                        if(currentPlace != null){
                            mLocationTextView.setText((
                                    getString(
                                            R.string.alamat_detail,
                                            currentPlace.getName(),
                                            result,
                                            System.currentTimeMillis())));
                        }
                        likelyPlaces.release();


                    }
                    else {
                        mLocationTextView.setText(
                                getString(
                                        R.string.alamat_detail,
                                        "nama lokasi tidak ditemukan",
                                        result,
                                        System.currentTimeMillis()));

                    }
                }
            });

        }

    }

    private void mulaiTrackingLokasi(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION
            );
        }else {

            mFusedLocationClient.requestLocationUpdates(getLocationRequest(),mLocationCalback,null);

            mLocationTextView.setText(getString(R.string.alamat_detail,"sedang mencari nama tempat","sedang mencari alamat",System.currentTimeMillis()));
            mTrackingLocation = true;
            mLocationButton.setText("Stop Tracking Lokasi");
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

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(DetailTempat.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();
            add = add + "\n" + obj.getFeatureName();

            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
