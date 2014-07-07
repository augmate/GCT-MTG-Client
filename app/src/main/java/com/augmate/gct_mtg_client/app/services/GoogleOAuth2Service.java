package com.augmate.gct_mtg_client.app.services;

import com.augmate.gct_mtg_client.app.services.models.DeviceAuthInfo;
import com.augmate.gct_mtg_client.app.services.models.GoogleTokenCredential;
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface GoogleOAuth2Service {

    @FormUrlEncoded
    @POST("/o/oauth2/device/code")
    public void getDeviceCode(@Field("client_id") String client_id,
                              @Field("scope") String scope,
                              Callback<DeviceAuthInfo> callback);

    @FormUrlEncoded
    @POST("/o/oauth2/token")
    public GoogleTokenCredential getToken(@Field("client_id") String client_id,
                         @Field("client_secret") String client_secret,
                         @Field("code") String code,
                         @Field("grant_type") String grant_type);

}
