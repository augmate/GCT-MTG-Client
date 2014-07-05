package com.augmate.gct_mtg_client.app;

import android.content.Context;
import android.util.Log;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.services.GoogleOAuth2Service;
import com.augmate.gct_mtg_client.app.services.models.DeviceAuthInfo;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class CredentialGen {

    public static final String AUTHORIZATION_CODE = "4/C11Z3U0yuhg2QJWaStdR2b7MIYzA.UrlwXH6vLfQb3oEBd8DOtNCfVtf0jQI";
    private Context context;
    private Reader secretReader;

    public CredentialGen(Context context) {
        this.context = context;
        this.secretReader = new InputStreamReader(context.getResources().openRawResource(R.raw.secret));
    }

    public void getAuthorization() {

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint("https://accounts.google.com")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        GoogleOAuth2Service service = adapter.create(GoogleOAuth2Service.class);

        service.getDeviceCode("314589339408-7d7dur5527ba6rikc7al54r3aa67p9ud.apps.googleusercontent.com",
                Joiner.on(' ').join(OAuth.SCOPES),
                new Callback<DeviceAuthInfo>() {
                    @Override
                    public void success(DeviceAuthInfo deviceAuthInfo, Response response) {
                        Log.d("com.augmate.auth", "device code :" + deviceAuthInfo.device_code);
                        Log.d("com.augmate.auth", "verify url  :" + deviceAuthInfo.verification_url);
                        Log.d("com.augmate.auth", "user code   :" + deviceAuthInfo.user_code);
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.e("com.augmate.auth", "device auth failed");
                    }
                });

    }

    public Credential getCreditials() {
        OAuth.setReader(secretReader);
        OAuth.setContext(context);

        try {
//          String authorizationUrl = OAuth.getAuthorizationUrl("johny@augmate.com", "");
//          Log.d("com.augmate.auth2", authorizationUrl);
            return OAuth.getCredentials(AUTHORIZATION_CODE);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (OAuth.CodeExchangeException e) {
            e.printStackTrace();
        }
        return null;
    }
}
