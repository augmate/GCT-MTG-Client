package com.augmate.gct_mtg_client.app;


import android.content.Context;
import android.content.SharedPreferences;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

// ...

class OAuth {
    private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/calendar",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile");

    private static GoogleAuthorizationCodeFlow flow = null;
    private static Reader reader;
    private static Context context;

    public static void setReader(Reader reader) {
        OAuth.reader = reader;
    }

    public static void setContext(Context context) {
        OAuth.context = context;
    }

    /**
     * Exception thrown when an error occurred while retrieving credentials.
     */
    public static class GetCredentialsException extends Exception {

        protected String authorizationUrl;

        /**
         * Construct a GetCredentialsException.
         *
         * @param authorizationUrl The authorization URL to redirect the user to.
         */
        public GetCredentialsException(String authorizationUrl) {
            this.authorizationUrl = authorizationUrl;
        }

        /**
         * @return the authorizationUrl
         */
    }

    /**
     * Exception thrown when a code exchange has failed.
     */
    public static class CodeExchangeException extends GetCredentialsException {

        /**
         * Construct a CodeExchangeException.
         *
         * @param authorizationUrl The authorization URL to redirect the user to.
         */
        public CodeExchangeException(String authorizationUrl) {
            super(authorizationUrl);
        }

    }

    /**
     * Exception thrown when no refresh token has been found.
     */
    public static class NoRefreshTokenException extends GetCredentialsException {

        /**
         * Construct a NoRefreshTokenException.
         *
         * @param authorizationUrl The authorization URL to redirect the user to.
         */
        public NoRefreshTokenException(String authorizationUrl) {
            super(authorizationUrl);
        }

    }

    /**
     * Exception thrown when no user ID could be retrieved.
     */
    private static class NoUserIdException extends Exception {
    }

    /**
     * Retrieved stored credentials for the provided user ID.
     *
     * @return Stored Credential if found, {@code null} otherwise.
     */
    static Credential getStoredCredentials() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), context.MODE_PRIVATE);
        String access_token = sharedPreferences.getString("access_token", null);
        String refresh_token = sharedPreferences.getString("refresh_token", null);

        return new GoogleCredential.Builder()
                .setClientSecrets("314589339408-7d7dur5527ba6rikc7al54r3aa67p9ud.apps.googleusercontent.com", "")
                .setJsonFactory(new JacksonFactory()).setTransport(AndroidHttp.newCompatibleTransport()).build()
                .setRefreshToken(refresh_token).setAccessToken(access_token);
    }

    /**
     * Store OAuth 2.0 credentials in the application's database.
     *
     * @param credentials The OAuth 2.0 credentials to store.
     */
    static void storeCredentials(Credential credentials) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getPackageName(), context.MODE_PRIVATE).edit();
        editor.putString("access_token", credentials.getAccessToken());
        editor.putString("refresh_token", credentials.getRefreshToken());
        editor.apply();
    }

    /**
     * Build an authorization flow and store it as a static class attribute.
     *
     * @return GoogleAuthorizationCodeFlow instance.
     * @throws IOException Unable to load client_secrets.json.
     */
    static GoogleAuthorizationCodeFlow getFlow() throws IOException {
        if (flow == null) {
            HttpTransport httpTransport = new NetHttpTransport();
            JacksonFactory jsonFactory = new JacksonFactory();
            GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(jsonFactory, reader);
            flow =
                    new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, SCOPES)
                            .setAccessType("offline").setApprovalPrompt("force").build();
        }
        return flow;
    }

    /**
     * Exchange an authorization code for OAuth 2.0 credentials.
     *
     * @param authorizationCode Authorization code to exchange for OAuth 2.0
     *        credentials.
     * @return OAuth 2.0 credentials.
     * @throws CodeExchangeException An error occurred.
     */
    static Credential exchangeCode(String authorizationCode)
            throws CodeExchangeException {
        try {
            GoogleAuthorizationCodeFlow flow = getFlow();
            GoogleTokenResponse response =
                    flow.newTokenRequest(authorizationCode).setRedirectUri(REDIRECT_URI).execute();
            return flow.createAndStoreCredential(response, null);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e);
            throw new CodeExchangeException(null);
        }
    }

    /**
     * Send a request to the UserInfo API to retrieve the user's information.
     *
     * @param credentials OAuth 2.0 credentials to authorize the request.
     * @return User's information.
     * @throws NoUserIdException An error occurred.
     */
    static Userinfoplus getUserInfo(Credential credentials)
            throws NoUserIdException {
        Oauth2 userInfoService =
                new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), credentials).build();
        Userinfoplus userInfo = null;
        try {
            userInfo = userInfoService.userinfo().get().execute();
        } catch (IOException e) {
            System.err.println("An error occurred: " + e);
        }
        if (userInfo != null && userInfo.getId() != null) {
            return userInfo;
        } else {
            throw new NoUserIdException();
        }
    }

    /**
     * Retrieve the authorization URL.
     *
     * @param userId User's Google ID.
     * @param state State for the authorization URL.
     * @return Authorization URL to redirect the user to.
     * @throws IOException Unable to load client_secrets.json.
     */
    public static String getAuthorizationUrl(String userId, String state) throws IOException {
        GoogleAuthorizationCodeRequestUrl urlBuilder =
                getFlow().newAuthorizationUrl().setRedirectUri(REDIRECT_URI).setState(state);
        urlBuilder.set("user_id", userId);
        return urlBuilder.build();
    }

    /**
     * Retrieve credentials using the provided authorization code.
     *
     * This function exchanges the authorization code for an access token and
     * queries the UserInfo API to retrieve the user's Google ID. If a
     * refresh token has been retrieved along with an access token, it is stored
     * in the application database using the user's Google ID as key. If no
     * refresh token has been retrieved, the function checks in the application
     * database for one and returns it if found or throws a NoRefreshTokenException
     * with the authorization URL to redirect the user to.
     *
     * @param authorizationCode Authorization code to use to retrieve an access
     *        token.
     * @return OAuth 2.0 credentials instance containing an access and refresh
     *         token.
     * @throws NoRefreshTokenException No refresh token could be retrieved from
     *         the available sources.
     * @throws IOException Unable to load client_secrets.json.
     */
    public static Credential getCredentials(String authorizationCode) throws IOException, CodeExchangeException {

        Credential credentials;

        try {
            credentials = getStoredCredentials();

            if (credentials == null || credentials.getRefreshToken() == null) {
                credentials = exchangeCode(authorizationCode);
                storeCredentials(credentials);
            }

            credentials.refreshToken();

            return credentials;

        } catch (CodeExchangeException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
