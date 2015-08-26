package com.autonomousapps.alexandriaplays.net;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Created by Tony Robalik (@AutonomousApps) on 8/25/15.
 */
public class ProjectPlayServiceTest {

    // Class under test
    ProjectPlayService mService;
    private List<Playground> mPlaygrounds;
    private boolean mFailure = true;

    @Before
    public void setUp() {
        mService = ProjectPlayService.instance();
    }

    @Test
    public void testGetAllPlaygrounds() throws Exception {
        // Make asynchronous method call
        mService.getAllPlaygrounds(new Callback<List<Playground>>() {
            @Override
            public void success(List<Playground> playgrounds, Response response) {
                mPlaygrounds = playgrounds;
                mFailure = false;
            }

            @Override
            public void failure(RetrofitError error) {
                mPlaygrounds = Collections.emptyList();
                mFailure = true;
            }
        });

        // Wait for network response
        await().atMost(5, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return mPlaygrounds != null;
                    }
                });

        // Assert
        String msg = mFailure ? "Network call failed" : "Network call succeeded";
        int numPlaygrounds = mPlaygrounds.size();
        Assert.assertThat(String.format("%s. Expected 86 playgrounds, but got <%d>", msg, numPlaygrounds),
                numPlaygrounds, equalTo(86));
    }
}