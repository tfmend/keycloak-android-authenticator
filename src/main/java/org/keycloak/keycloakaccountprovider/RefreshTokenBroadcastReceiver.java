package org.keycloak.keycloakaccountprovider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RefreshTokenBroadcastReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 901;

    public RefreshTokenBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        RefreshTokenService.startActionRefreshToken(context);
    }
}
