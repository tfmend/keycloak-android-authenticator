package org.keycloak.keycloakaccountprovider;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by thiago on 9/12/15.
 */
class SessionManager {

    private Activity activity;

    public SessionManager(Activity activity) {
        this.activity = activity;

        Intent i = new Intent(activity.getApplicationContext(), RefreshTokenBroadcastReceiver
                .class);

        final PendingIntent pI = PendingIntent.getBroadcast(activity,
                RefreshTokenBroadcastReceiver.REQUEST_CODE, i, PendingIntent
                        .FLAG_UPDATE_CURRENT);

        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.activity.getSystemService(Context
                .ALARM_SERVICE);

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager
                .INTERVAL_HALF_HOUR, pI);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return activity;
    }
}
