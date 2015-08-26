package com.autonomousapps.alexandriaplays.net;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;

/**
 * Created by Tony Robalik (@AutonomousApps) on 8/25/15. Encapsulates this functionality in a pure
 * Java class to permit unit testing.
 */
public class ProjectPlayService {

    // Singleton
    private static ProjectPlayService INSTANCE;

    // Retrofit service
    private IProjectPlayService mService = null;

    private ProjectPlayService() {
        if (mService != null) return;

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(IProjectPlayService.ENDPOINT)
                .build();
        mService = restAdapter.create(IProjectPlayService.class);
    }

    public static ProjectPlayService instance() {
        if (INSTANCE == null) {
            synchronized (ProjectPlayService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ProjectPlayService();
                }
            }
        }
        return INSTANCE;
    }

    public void getAllPlaygrounds(Callback<List<Playground>> callback) {
        mService.getAllPlaygrounds(callback);
    }
}