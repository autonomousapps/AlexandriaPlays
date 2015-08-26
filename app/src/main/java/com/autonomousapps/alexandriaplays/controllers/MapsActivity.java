package com.autonomousapps.alexandriaplays.controllers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.autonomousapps.alexandriaplays.R;
import com.autonomousapps.alexandriaplays.net.PhotoTask;
import com.autonomousapps.alexandriaplays.net.Playground;
import com.autonomousapps.alexandriaplays.net.ProjectPlayService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.Places;
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
        OnMarkerClickListener, OnMapClickListener,
        ConnectionCallbacks, OnConnectionFailedListener {
    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    // Model
    private List<Playground> mPlaygrounds;
    private Map<String, Playground> mPlaygroundMap;

    private boolean mPlaygroundsLoaded = false;

    // Place info
    @Bind(R.id.img_logo) protected ImageView mImgLogo;
    @Bind(R.id.sliding_layout) protected SlidingUpPanelLayout mSlidingUpPanelLayout;
    @Bind(R.id.img_place) protected ImageView mImgPlace;
    @Bind(R.id.text_attribution) protected TextView mTextAttr;
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

        // Initialize important things
        buildGoogleApiClient();
        getPlaygrounds();

        // Get map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void getPlaygrounds() {
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
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
//                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void loadPlaygrounds() {
        if (mMap == null || mPlaygrounds == null || mPlaygroundsLoaded) return;

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

        mPlaygroundsLoaded = true;
    }

    private String yesOrNo(boolean b) {
        return b ? "Yes" : "No";
    }

    private void setImageFromGooglePlace(String placeId) {
        // Don't try to access API if it's not connected
        if (!mGoogleApiClient.isConnected()) return;

        new PhotoTask(mGoogleApiClient, mImgPlace.getWidth(), mImgPlace.getHeight()) {
            @Override
            protected void onPostExecute(AttributedPhoto attributedPhoto) {
                Log.d(TAG, "onPostExecute()");
                if (attributedPhoto != null) {
                    Log.d(TAG, "photo is not null");
                    // Photo has been loaded, display it.
                    mImgPlace.setImageBitmap(attributedPhoto.bitmap);

                    // Display the attribution as HTML content if set.
                    if (attributedPhoto.attribution == null) {
                        mTextAttr.setVisibility(View.GONE);
                    } else {
                        mTextAttr.setVisibility(View.VISIBLE);
                        mTextAttr.setText(Html.fromHtml(attributedPhoto.attribution.toString()));
                    }
                }
            }
        }.execute(placeId);
    }

    private void enableSlidingPanel(boolean enable) {
        if (enable) {
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }
    }

    /*
     * Google Maps API callbacks
     */

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

        loadPlaygrounds();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // zoom in to marker
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));

        // Display info window
        enableSlidingPanel(true);
        Playground playground = mPlaygroundMap.get(marker.getTitle());
        mTextName.setText(playground.getName());
        mTextRestrooms.setText("Restrooms: " + yesOrNo(playground.getRestrooms() > 0));
        mTextSeating.setText("Seating: " + yesOrNo(playground.getSeating() > 0));
        mTextAgeLevel.setText("Age Level: " + playground.getAgelevel());
        mTextShade.setText("Shade: " + yesOrNo(playground.getShade() > 0));
        mTextWater.setText("Water fountains: " + yesOrNo(playground.getDrinkingw() > 0));

        // Get image of place
        setImageFromGooglePlace(playground.getGooglePlacesid());

        // Consume event
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        enableSlidingPanel(false);
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

    /*
     * Google Places API callbacks.
     */

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google API connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google API connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Google API connection failed");
    }
}