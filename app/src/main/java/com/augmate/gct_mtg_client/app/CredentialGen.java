package com.augmate.gct_mtg_client.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.augmate.gct_mtg_client.app.activities.DeviceAuthInfoActivity;
import com.augmate.gct_mtg_client.app.services.GoogleOAuth2Service;
import com.augmate.gct_mtg_client.app.services.models.DeviceAuthInfo;
import com.augmate.gct_mtg_client.app.services.models.GoogleTokenCredential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

// Not thread safe by any stretch of the imagination
public class CredentialGen {
    public static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/calendar",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile");

    private static final String CLIENT_ID = "314589339408-q9e0q18opa260t2ru18t6kklrsu1hn0p.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "u7yOA3IGmOfioJL8ENjQsNFh";
    private static final String GRANT_TYPE = "http://oauth.net/grant_type/device/1.0";
    private static final String GOOGLE_ACCOUNTS_HOSTNAME = "https://accounts.google.com";

    private final GoogleOAuth2Service service;
    private Context context;

    public CredentialGen(Context context) {
        this.context = context;

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(GOOGLE_ACCOUNTS_HOSTNAME)
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

        GoogleCredential googleCredential = null;

        if(isNullOrEmpty(token.error)){
            googleCredential = buildGoogleCredentials(token.access_token, token.refresh_token);
            storeCredentials(googleCredential);
        }

        return googleCredential;
    }

    public void getAuthorization() {
        Log.i("com.augmate.auth", "authorizing device");

        service.getDeviceCode(CLIENT_ID,
                Joiner.on(' ').join(SCOPES),
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
                        Intent i = new Intent(context, DeviceAuthInfoActivity.class);
                        i.putExtra("verification_url", deviceAuthInfo.verification_url);
                        i.putExtra("user_code", deviceAuthInfo.user_code);
                        context.startActivity(i);
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.e("com.augmate.auth", "device auth failed");
                    }
                });

    }

    public GoogleCredential getCreditials() {
        GoogleCredential googleCredential = null;

        String device_code = context
                .getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                .getString("device_code", "");

        Log.d("com.augmate.auth", "device_code from storage: " + device_code);

        try {
            googleCredential = getStoredCredentials();

            if (googleCredential.getRefreshToken() == null) {
                Log.i("com.augmate.auth", "Getting new tokens, refresh token not stored");
                googleCredential = getToken(device_code);
            } else {
                Log.i("com.augmate.auth", "Found tokens, refreshing them");
                // TODO: Refresh token expires, need to handle
                googleCredential.refreshToken();
            }
        } catch (IOException e) {
            // empty
        } catch (RetrofitError e) {
            // empty
        } finally {
            if(googleCredential == null || isNullOrEmpty(googleCredential.getRefreshToken())){
                getAuthorization();
            }
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
