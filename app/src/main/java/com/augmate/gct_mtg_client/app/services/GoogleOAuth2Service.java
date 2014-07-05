package com.augmate.gct_mtg_client.app.services;

import com.augmate.gct_mtg_client.app.services.models.DeviceAuthInfo;
import retrofit.Callback;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;

public interface GoogleOAuth2Service {

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/o/oauth2/device/code")
    public void getDeviceCode(@Query("client_id") String client_id, @Query("scope") String scope, Callback<DeviceAuthInfo> callback);

}
