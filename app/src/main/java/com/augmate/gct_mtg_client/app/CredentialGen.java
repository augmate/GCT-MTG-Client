package com.augmate.gct_mtg_client.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.augmate.gct_mtg_client.app.services.GoogleOAuth2Service;
import com.augmate.gct_mtg_client.app.services.models.DeviceAuthInfo;
import com.augmate.gct_mtg_client.app.services.models.GoogleTokenCredential;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.IOException;

public class CredentialGen {
    private static final String CLIENT_ID = "314589339408-q9e0q18opa260t2ru18t6kklrsu1hn0p.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "u7yOA3IGmOfioJL8ENjQsNFh";
    private static final String GRANT_TYPE = "http://oauth.net/grant_type/device/1.0";

    private final GoogleOAuth2Service service;
    private Context context;

    public CredentialGen(Context context) {
        this.context = context;

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint("https://accounts.google.com")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        service = adapter.create(GoogleOAuth2Service.class);
    }

    public GoogleCredential getToken(String deviceCode) {

        GoogleTokenCredential token = service.getToken(
                CLIENT_ID,
                CLIENT_SECRET,
                deviceCode,
                GRANT_TYPE);

        GoogleCredential googleCredential = buildGoogleCredentials(token.access_token, token.refresh_token);

        storeCredentials(googleCredential);

        return googleCredential;
    }

    public void getAuthorization() {

        service.getDeviceCode(CLIENT_ID,
                Joiner.on(' ').join(OAuth.SCOPES),
                new Callback<DeviceAuthInfo>() {
                    @Override
                    public void success(DeviceAuthInfo deviceAuthInfo, Response response) {
                        Log.d("com.augmate.auth", "device code : " + deviceAuthInfo.device_code);
                        Log.d("com.augmate.auth", "verify url  : " + deviceAuthInfo.verification_url);
                        Log.d("com.augmate.auth", "user code   : " + deviceAuthInfo.user_code);

                        context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                                .edit()
                                .putString("device_code", deviceAuthInfo.device_code)
                                .apply();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.e("com.augmate.auth", "device auth failed");
                    }
                });

    }

    public Credential getCreditials() {
        GoogleCredential googleCredential = null;

        String device_code = context
                .getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                .getString("device_code", "");

        Log.d("com.augmate.auth", "device_code from storage: " + device_code);

        try {

            if (device_code != "") {

                googleCredential = getStoredCredentials();

                if (googleCredential.getRefreshToken() == null) {
                    Log.i("com.augmate.auth", "Getting new tokens, none stored");
                    googleCredential = getToken(device_code);
                } else {
                    Log.i("com.augmate.auth", "Found tokens, refreshing them");
                    googleCredential.refreshToken();
                }

            } else {
                getAuthorization();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleCredential;
    }


    private void storeCredentials(GoogleCredential credentials) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getPackageName(), context.MODE_PRIVATE).edit();
        editor.putString("access_token", credentials.getAccessToken());
        editor.putString("refresh_token", credentials.getRefreshToken());
        editor.apply();
    }

    private GoogleCredential getStoredCredentials() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), context.MODE_PRIVATE);
        String access_token = sharedPreferences.getString("access_token", null);
        String refresh_token = sharedPreferences.getString("refresh_token", null);

        return buildGoogleCredentials(access_token, refresh_token);
    }

    private GoogleCredential buildGoogleCredentials(String access_token, String refresh_token) {
        return new GoogleCredential.Builder()
                .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                .setJsonFactory(new GsonFactory()).setTransport(AndroidHttp.newCompatibleTransport()).build()
                .setRefreshToken(refresh_token)
                .setAccessToken(access_token);
    }
}
