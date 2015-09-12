package org.keycloak.keycloakaccountprovider;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RefreshTokenService extends IntentService {
    private static final String ACTION_REFRESH_TOKEN = "org.keycloak.keycloakaccountprovider" +
            ".action.refreshtoken";
    public static final String TAG = "RefreshTokenService";

    /**
     * Starts this service to perform action Refresh Token with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRefreshToken(Context context) {
        Intent intent = new Intent(context, RefreshTokenService.class);
        intent.setAction(ACTION_REFRESH_TOKEN);
        //intent.putExtra(EXTRA_PARAM1, param1);
        context.startService(intent);
    }

    public RefreshTokenService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            handleActionRefreshToken();
        }
        Log.i(TAG, "Service running");
    }

    /**
     * Handle action Refresh Token in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRefreshToken() {
        Log.d(TAG, "Not yet implemented");
    }

}
