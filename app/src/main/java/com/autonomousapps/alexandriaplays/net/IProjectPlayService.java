package com.autonomousapps.alexandriaplays.net;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Tony Robalik (@AutonomousApps) on 8/24/15.
 */
public interface IProjectPlayService {
    String ENDPOINT = "http://projectplay.herokuapp.com/playgrounds";

    @GET("/getPlaygrounds.json")
    void getPlaygrounds(@Query("address") String address, @Query("radius") int radius,
                        Callback<List<Playground>> callback);

    @GET("/page.json/1/1000")
    void getAllPlaygrounds(Callback<List<Playground>> callback);

//    @GET("/getPlacesURLforPlayground.json?name=Simpson%20Stadium%20Park")
//    void getGooglePlace();
}