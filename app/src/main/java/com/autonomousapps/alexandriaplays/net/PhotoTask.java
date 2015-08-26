package com.autonomousapps.alexandriaplays.net;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

/**
 * Created by Tony Robalik (@AutonomousApps) on 8/26/15.
 */
public abstract class PhotoTask extends AsyncTask<String, Void, PhotoTask.AttributedPhoto> {
    private static final String TAG = PhotoTask.class.getSimpleName();

    final private GoogleApiClient mGoogleApiClient;

    private int mHeight;
    private int mWidth;

    public PhotoTask(GoogleApiClient googleApiClient, int width, int height) {
        mGoogleApiClient = googleApiClient;
        mHeight = height;
        mWidth = width;
    }

    /**
     * Loads the first photo for a place id from the Geo Data API.
     * The place id must be the first (and only) parameter.
     */
    @Override
    protected AttributedPhoto doInBackground(String... params) {
        if (params.length != 1) {
            return null;
        }
        final String placeId = params[0];
        Log.d(TAG, "doInBackground(" + placeId + ")");

        AttributedPhoto attributedPhoto = null;

        PlacePhotoMetadataResult result = Places.GeoDataApi
                .getPlacePhotos(mGoogleApiClient, placeId).await();

        if (result.getStatus().isSuccess()) {
            PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
            Log.d(TAG, String.format("Result successful. There are %d photos", photoMetadataBuffer.getCount()));

            if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                // Get the first bitmap and its attributions.
                PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                CharSequence attribution = photo.getAttributions();
                // Load a scaled bitmap for this photo.
                Bitmap image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                        .getBitmap();

                attributedPhoto = new AttributedPhoto(attribution, image);
            }
            // Release the PlacePhotoMetadataBuffer.
            photoMetadataBuffer.release();
        } else {
            Log.d(TAG, "Result unsuccessful");
        }
        return attributedPhoto;
    }

    @Override
    protected abstract void onPostExecute(AttributedPhoto attributedPhoto);

    /**
     * Holder for an image and its attribution.
     */
    public class AttributedPhoto {

        public final CharSequence attribution;

        public final Bitmap bitmap;

        public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
            this.attribution = attribution;
            this.bitmap = bitmap;
        }
    }
}