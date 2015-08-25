package com.autonomousapps.alexandriaplays.net;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;

/**
 * Created by Tony Robalik (@AutonomousApps) on 8/25/15. Encapsulates this functionality in a pure
 * Java class to permit easier unit testing.
 */
public enum ProjectPlayServiceImpl {

    // Singleton
    INSTANCE;

    // Retrofit service
    private ProjectPlayService mService;

    ProjectPlayServiceImpl() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ProjectPlayService.ENDPOINT)
                .build();
        mService = restAdapter.create(ProjectPlayService.class);
    }

    public void getAllPlaygrounds(Callback<List<Playground>> callback) {
        mService.getAllPlaygrounds(callback);
    }
}