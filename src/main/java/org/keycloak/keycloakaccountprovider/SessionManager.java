package org.keycloak.keycloakaccountprovider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Date;

/**
 * Created by thiago on 9/12/15.
 */
class SessionManager {

    private static final String TAG = SessionManager.class.getCanonicalName();

    private Activity activity;
    private AccountManager am;

    public SessionManager(Activity activity) {
        this.activity = activity;
        am = AccountManager.get(activity);

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

    public boolean isConnected() {
        Account[] accounts = am.getAccountsByType(this.activity.getString(R.string.account_type));

        if (accounts.length == 0) {
            return false;
        }

        String keyCloackAccount = am.getUserData(accounts[0], this.activity.getString(R.string
                .account_key));
        KeyCloakAccount kcAccount = new Gson().fromJson(keyCloackAccount, KeyCloakAccount.class);

        return new Date(kcAccount.getExpiresOn()).after(new Date());
    }

    private String getAuthToken() {
        Account[] accounts = this.am.getAccountsByType(this.activity.getString(R.string
                .account_type));
        if (accounts.length == 0) {
            return null;
        }
        Account first = accounts[0];

        final String[] token = new String[1];
        AccountManagerFuture<Bundle> future = this.am.getAuthToken(first, this
                .activity.getString(R.string.account_type), null, this.activity, new
                AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
                        try {
                            String accessToken = accountManagerFuture.getResult()
                                    .getString(AccountManager.KEY_AUTHTOKEN);
                            token[0] = accessToken;
                        } catch (OperationCanceledException e) {
                            token[0] = null;
                        } catch (IOException e) {
                            token[0] = null;
                        } catch (AuthenticatorException e) {
                            token[0] = null;
                        }
                    }
                }, null);

        return token[0];
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, TAG + ".onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
    }
}
