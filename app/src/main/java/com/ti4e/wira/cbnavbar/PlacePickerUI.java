package com.ti4e.wira.cbnavbar;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class PlacePickerUI extends AppCompatActivity implements DapatkanAlamatTask.onTaskSelesai{
    Button mLocationButton,mPlacePickerButton;
    private Location mLastLocation;
    TextView mLocationTextView;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION =1;
    private static final int REQUEST_PICK_PLACE =0;
    private ImageView mAndroidImageView;
    private AnimatorSet mRotateAnim;

    private PlaceDetectionClient mPlaceDetectionClient;
    private String mLastPlaceName;

    private boolean mTrackingLocation;
    private LocationCallback mLocationCalback;
    public static int drawable = R.drawable.airport;
    String nama="",alamat="";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //syntax untuk menyimpan instance berupa string dan integer
        outState.putInt("gambar",drawable);
        outState.putString("nama",nama);
        outState.putString("alamat",alamat);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getString("gambar") == ""){
            mLocationTextView.setText("Pijet tombol untuk dapatkan lokasi anda");
        }else{
//syntax untuk merestore instance yang sebelumnya tersimpan
            mAndroidImageView.setImageResource(savedInstanceState.getInt("gambar"));
            mLocationTextView.setText(getString(R.string.alamat_detail,savedInstanceState.getString("nama"),savedInstanceState.getString("alamat"),System.currentTimeMillis()));
        }

    }

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

        mPlacePickerButton = (Button) findViewById(R.id.button_placepicker);



        mLocationCalback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mTrackingLocation){
                    new DapatkanAlamatTask(PlacePickerUI.this,PlacePickerUI.this).execute(locationResult.getLastLocation());
                }
            }
        };
        mPlacePickerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder= new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(PlacePickerUI.this),REQUEST_PICK_PLACE);
                }catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e ){
                    e.printStackTrace();
                }
            }
        });

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
                            setTipeLokasi(currentPlace);
                            //code dibawah ini berfungsi untuk mengisi nilai variable yang akan disimpan di savedInstance
                            drawable = setTipeLokasi(currentPlace);
                            nama = currentPlace.getName().toString();
                            alamat = result.toString();
                            mAndroidImageView.setImageResource(drawable);
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

    private int  setTipeLokasi(Place currentPlace){
        int drawableId= -1;
        for (Integer placeType : currentPlace.getPlaceTypes()){
            switch (placeType){
                case Place.TYPE_UNIVERSITY:
                    drawableId = R.drawable.kampus;
                    break;
                case Place.TYPE_CAFE:
                    drawableId = R.drawable.warkop;
                    break;
                case Place.TYPE_SHOPPING_MALL:
                    drawableId = R.drawable.toko;
                    break;
                case Place.TYPE_MOVIE_THEATER:
                    drawableId = R.drawable.bioskop;
                    break;
                //tambahan
                case Place.TYPE_AIRPORT:
                    drawableId = R.drawable.airport;
                    break;
                case Place.TYPE_ATM:
                    drawableId = R.drawable.atm;
                    break;
                case Place.TYPE_MOSQUE:
                    drawableId = R.drawable.mosque;
                    break;
            }
        }
        if(drawableId < 0){
            drawableId = R.drawable.unknown;
        }
        mAndroidImageView.setImageResource(drawableId);
        drawable = drawableId;
        return drawableId;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            Place place = PlacePicker.getPlace(this,data);
            setTipeLokasi(place);
            mLocationTextView.setText(getString(R.string.alamat_detail,place.getName(),place.getAddress(),System.currentTimeMillis()));
        }else {
            mLocationTextView.setText("kok ga dipilih");
        }

    }

    public int getDrawable() {
        return drawable;
    }
}
