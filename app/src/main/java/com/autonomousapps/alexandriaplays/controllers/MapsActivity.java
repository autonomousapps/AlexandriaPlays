package com.autonomousapps.alexandriaplays.controllers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.autonomousapps.alexandriaplays.R;
import com.autonomousapps.alexandriaplays.net.Playground;
import com.autonomousapps.alexandriaplays.net.ProjectPlayService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MapsActivity extends Activity implements OnMapReadyCallback, OnMapLoadedCallback,
        OnMarkerClickListener, OnMapClickListener {
    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;

    // Model
    private List<Playground> mPlaygrounds;
    private Map<String, Playground> mPlaygroundMap;

    // Place info
    @Bind(R.id.img_logo) protected ImageView mImgLogo;
    @Bind(R.id.sliding_layout) protected SlidingUpPanelLayout mSlidingUpPanelLayout;
    @Bind(R.id.text_name) protected TextView mTextName;
    @Bind(R.id.text_restrooms) protected TextView mTextRestrooms;
    @Bind(R.id.text_water) protected TextView mTextWater;
    @Bind(R.id.text_seating) protected TextView mTextSeating;
    @Bind(R.id.text_shade) protected TextView mTextShade;
    @Bind(R.id.text_age_level) protected TextView mTextAgeLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        // UI
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        // Retrofit
        ProjectPlayService.instance().getAllPlaygrounds(new Callback<List<Playground>>() {
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

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLoadedCallback(this);

        // Add map marker for Alexandria
        LatLng alexandria = new LatLng(Double.parseDouble(getString(R.string.alexandria_lat)),
                Double.parseDouble(getString(R.string.alexandria_long)));
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
        enableSlidingPanel(true);
        Playground playground = mPlaygroundMap.get(marker.getTitle());
        mTextName.setText(playground.getName());
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
        enableSlidingPanel(false);
    }

    private void enableSlidingPanel(boolean enable) {
        if (enable) {
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }
    }

    /**
     * Called when all the tiles necessary to render the map have loaded.
     */
    @Override
    public void onMapLoaded() {
        Log.d(TAG, "onMapLoaded()");
        // TODO this is the world's latest splash screen
        mImgLogo.animate().alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                mImgLogo.setVisibility(View.GONE);
            }
        });
    }
}