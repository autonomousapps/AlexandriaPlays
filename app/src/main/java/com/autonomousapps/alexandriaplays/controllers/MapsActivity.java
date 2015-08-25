package com.autonomousapps.alexandriaplays.controllers;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.autonomousapps.alexandriaplays.R;
import com.autonomousapps.alexandriaplays.net.Playground;
import com.autonomousapps.alexandriaplays.net.ProjectPlayService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        OnMarkerClickListener, OnMapClickListener {
    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private ProgressDialog mProgress;

    private ProjectPlayService mService;
    private List<Playground> mPlaygrounds;
    private Map<String, Playground> mPlaygroundMap;

    // Place info
    @Bind(R.id.layout_place_info) protected ViewGroup mPlaceInfo;
    @Bind(R.id.text_restrooms) protected TextView mTextRestrooms;
    @Bind(R.id.text_water) protected TextView mTextWater;
    @Bind(R.id.text_seating) protected TextView mTextSeating;
    @Bind(R.id.text_shade) protected TextView mTextShade;
    @Bind(R.id.text_age_level) protected TextView mTextAgeLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        Log.d(TAG, "onCreate()");

        // Retrofit
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ProjectPlayService.ENDPOINT)
                .build();
        mService = restAdapter.create(ProjectPlayService.class);

        // Get playgrounds
        mService.getAllPlaygrounds(new Callback<List<Playground>>() {
            @Override
            public void success(List<Playground> playgrounds, Response response) {
                Log.v(TAG, "Retrofit successful in getting playgrounds. Returned: " + playgrounds.size());
                mPlaygrounds = playgrounds;
                mPlaygroundMap = new HashMap<>(mPlaygrounds.size());
                loadPlaygrounds();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Retrofit failed to get playgrounds for reason: " +
                        error.getLocalizedMessage());
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // SupportMapFragment is for use on devices at API level < 12
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Start progress indicator
        mProgress = ProgressDialog.show(this, "Loading map", "Loading map msg", true);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");

        // Remove progress indicator
        mProgress.dismiss();

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);

        // Add map marker for Alexandria
        LatLng alexandria = new LatLng(Double.parseDouble(getString(R.string.alexandria_lat)), Double.parseDouble(getString(R.string.alexandria_long)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(alexandria, 12));
    }

    private void loadPlaygrounds() {
        if (mMap == null) return;

        Log.v(TAG, "Loading playgrounds");
        for (Playground playground : mPlaygrounds) {
            String title = playground.getName();

            // Remember for later
            mPlaygroundMap.put(title, playground);

            // Add map marker
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(playground.getLat(), playground.getLong()))
                    .title(title));
        }
    }

    private String yesOrNo(boolean b) {
        return b ? "Yes" : "No";
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // zoom in to marker
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 18));

        // Display info window
        mPlaceInfo.setVisibility(View.VISIBLE);
        Playground playground = mPlaygroundMap.get(marker.getTitle());
        mTextRestrooms.setText("Restrooms: " + yesOrNo(playground.getRestrooms() > 0));
        mTextSeating.setText("Seating: " + yesOrNo(playground.getSeating() > 0));
        mTextAgeLevel.setText("Age Level: " + playground.getAgelevel());
        mTextShade.setText("Shade: " + yesOrNo(playground.getShade() > 0));
        mTextWater.setText("Water fountains: " + yesOrNo(playground.getDrinkingw() > 0));

        // Consume event
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mPlaceInfo.setVisibility(View.GONE);
    }
}