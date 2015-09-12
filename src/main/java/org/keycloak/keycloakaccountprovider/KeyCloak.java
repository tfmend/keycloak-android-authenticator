package org.keycloak.keycloakaccountprovider;

import android.content.Context;
import android.content.SharedPreferences;

import org.keycloak.keycloakaccountprovider.util.IOUtils;

import java.util.UUID;

/**
 * Created by Summers on 9/13/2014.
 */
public class KeyCloak {

    private final KeyCloakConfig config;
    private final Context context;

    public KeyCloak(Context context) {
        this.config = KeyCloakConfig.getInstance(context);
        this.context = context.getApplicationContext();
    }

    public String createLoginUrl() {
        String state = UUID.randomUUID().toString();
        String redirectUri = getRedirectUri();

        saveState(state);

        String url = config.getRealmUrl()
                + "/tokens/login"
                + "?client_id=" + IOUtils.encodeURIComponent(config.getClientId())
                + "&redirect_uri=" + IOUtils.encodeURIComponent(redirectUri)
                + "&state=" + IOUtils.encodeURIComponent(state)
                + "&response_type=code";


        return url;
    }

    private void saveState(String state) {
        SharedPreferences prefs = context.getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        prefs.edit().putString("state", state).commit();
    }

    public String getClientId() {
        return config.getClientId();
    }

    public String getRedirectUri() {
        return this.context.getResources().getString(R.string.callback);
    }

    public String getClientSecret() {
        return config.getClientSecret();
    }

    public String getBaseURL() {
        return config.getRealmUrl();
    }
}
