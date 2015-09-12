package org.keycloak.keycloakaccountprovider;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;

/**
 * Created by thiago on 9/1/15.
 */
public class KeyCloakConnectionResult {

    public static final int SUCCESS = 0;
    public static final int SIGN_IN_REQUIRED = 1;
    public static final int SIGN_IN_FAILED = 2;
    public static final int CANCELED = 3;
    public static final int CONN_ERROR = 4;
    public static final int AUTH_EXCEPTION = 5;

    private final int statusCode;

    public KeyCloakConnectionResult(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean hasResolution() {
        return this.statusCode != SUCCESS;
    }

    private String getStatusString(int statusCode) {
        switch (statusCode) {
            case SUCCESS:
                return "SUCCESS";
            case SIGN_IN_REQUIRED:
                return "SIGN_IN_REQUIRED";
            case SIGN_IN_FAILED:
                return "SIGN_IN_FAILED";
            case CANCELED:
                return "CANCELED";
            case CONN_ERROR:
                return "CONN_ERROR";
            case AUTH_EXCEPTION:
                return "AUTH_EXCEPTION";
            default:
                return "UNKNOWN_ERROR_CODE("+ statusCode +")";
        }
    }

    @Override
    public String toString() {
        return "KeyCloakConnectionResult{" +
                "statusCode=" + statusCode +
                '}';
    }
}
