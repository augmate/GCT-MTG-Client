package com.augmate.gct_mtg_client.app;

import android.content.Context;
import com.augmate.gct_mtg_client.R;
import com.google.api.client.auth.oauth2.Credential;

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

    public Credential getCreditials(){
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
