package org.keycloak.keycloakaccountprovider.util;

import android.accounts.NetworkErrorException;
import android.util.Base64;
import android.util.Log;

//import org.jboss.aerogear.android.http.HeaderAndBody;
//import org.jboss.aerogear.android.http.HttpException;
//import org.jboss.aerogear.android.http.HttpProvider;
//import org.jboss.aerogear.android.impl.http.HttpRestProvider;
import org.json.JSONException;
import org.json.JSONObject;
import org.keycloak.keycloakaccountprovider.KeyCloak;
import org.keycloak.keycloakaccountprovider.KeyCloakAccount;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public final class TokenExchangeUtils {

    private static final String TAG = TokenExchangeUtils.class.getSimpleName();

    private TokenExchangeUtils() {
    }

    public static KeyCloakAccount exchangeForAccessCode(String accessToken, KeyCloak kc) {

        final Map<String, String> data = new HashMap<String, String>();
        data.put("code", accessToken);
        data.put("client_id", kc.getClientId());
        data.put("redirect_uri", kc.getRedirectUri());

        data.put("grant_type", "authorization_code");
        if (kc.getClientSecret() != null) {
            data.put("client_secret", kc.getClientSecret());
        }

        try {
            URL accessTokenEndpoint = new URL(kc.getBaseURL() + "/tokens/access/codes");

            if (kc.getClientSecret() == null) {
                accessTokenEndpoint = new URL(kc.getBaseURL() + "/tokens/access/codes&client_id=" + IOUtils.encodeURIComponent(kc.getClientId()));
            }

            String bodyString = getBody(data);

            //HttpRestProvider provider = getHttpProvider(kc, accessTokenEndpoint);
            //HeaderAndBody result = provider.post(bodyString);

            HttpURLConnection conn = getHttpProvider(kc, accessTokenEndpoint);
            String result = post(conn, bodyString);

            JSONObject accessResponse = handleResult(result);
            KeyCloakAccount account = new KeyCloakAccount();
            account.extractTokenProperties(accessResponse);

            return account;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    private static String post(HttpURLConnection conn, String bodyString) throws IOException {
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Length", Integer.toString(bodyString.getBytes("UTF-8").length));
        conn.setDoInput(true);
        conn.setDoOutput(true);
        //conn.connect();

        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

        Log.i(TokenExchangeUtils.class.getName(),"Request Body : " + bodyString);

        writer.write(bodyString);
        writer.flush();

        int responseCode = conn.getResponseCode();

        Log.i(TokenExchangeUtils.class.getName(),"Response Code : " + responseCode);

        InputStream stream = conn.getInputStream();

        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        StringBuilder result = new StringBuilder();
        char[] buffer = new char[1];
        while (reader.read(buffer,0,buffer.length) >= 0) {
            result.append(buffer);
        }

        writer.close();
        reader.close();

        Log.i(TokenExchangeUtils.class.getName(), "Response : " + result.toString());

        return result.toString();
    }

    public static KeyCloakAccount refreshToken(KeyCloakAccount account, KeyCloak kc) throws NetworkErrorException {
        final Map<String, String> data = new HashMap<String, String>();
        data.put("refresh_token", account.getRefreshToken());
        data.put("grant_type", "refresh_token");

        Log.i(TokenExchangeUtils.class.toString(),"Refreshing : " + account.toString());

        try {
            URL refreshTokenEndpoint = new URL(kc.getBaseURL() + "/tokens/refresh");

            if (kc.getClientSecret() == null) {
                refreshTokenEndpoint = new URL(kc.getBaseURL() + "/tokens/refresh&client_id=" + IOUtils.encodeURIComponent(kc.getClientId()));
            }

            Log.i(TokenExchangeUtils.class.toString(),"Refreshing URL : " + refreshTokenEndpoint);

            String bodyString = getBody(data);

            //HttpRestProvider provider = getHttpProvider(kc, refreshTokenEndpoint);
            //HeaderAndBody result = provider.post(bodyString);

            HttpURLConnection conn = getHttpProvider(kc, refreshTokenEndpoint);
            String result = post(conn, bodyString);

            JSONObject accessResponse = handleResult(result);
            account.extractTokenProperties(accessResponse);

            return account;
        //} catch (IOException e) {
        //    throw e;
        } catch (Exception e) {
            throw new NetworkErrorException("Error Refreshing token",e);
        }
    }

    private static JSONObject handleResult(String body) {
        //byte[] bodyData = result.getBody();
        //String body = new String(bodyData);
        try {
            return new JSONObject(body);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static String getBody(Map<String, String> data) {
        final StringBuilder bodyBuilder = new StringBuilder();
        final String formTemplate = "%s=%s";

        String amp = "";
        for (Map.Entry<String, String> entry : data.entrySet()) {
            bodyBuilder.append(amp);
            try {
                bodyBuilder.append(String.format(formTemplate, entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            amp = "&";
        }

        return  bodyBuilder.toString();

    }

    private static HttpURLConnection getHttpProvider(KeyCloak kc, URL url) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        if (kc.getClientSecret() != null) {
            try {
                conn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((kc.getClientId() + ":" + kc.getClientSecret()).getBytes("UTF-8"), Base64.DEFAULT | Base64.NO_WRAP));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        return conn;

//        HttpRestProvider  provider  = new HttpRestProvider(url);
//
//        provider.setDefaultHeader("Content-Type", "application/x-www-form-urlencoded");
//
//        if (kc.getClientSecret() != null) {
//            try {
//                provider.setDefaultHeader("Authorization", "Basic " + Base64.encodeToString((kc.getClientId() + ":" + kc.getClientSecret()).getBytes("UTF-8"), Base64.DEFAULT | Base64.NO_WRAP));
//            } catch (UnsupportedEncodingException e) {
//                Log.e(TAG, e.getMessage(), e);
//                throw new RuntimeException(e);
//            }
//        }
//        return provider;
    }

}
