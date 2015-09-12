package org.keycloak.keycloakaccountprovider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;

/**
 * Created by tfmend on 8/25/15.
 */
public class KeyCloakApiClient {

    private static final String TAG = KeyCloakApiClient.class.getCanonicalName();

    private static AccountManager am = null;
    private static KeyCloakApiClient instance = null;
    private static Activity context = null;
    private static SessionManager mSessionManager = null;

    private KeyCloakApiClient(Activity activity) {
        context = activity;
    }

    public static KeyCloakApiClient getInstance(Activity activity) {
        if (instance == null) {
            instance = new KeyCloakApiClient(activity);
            mSessionManager = new SessionManager(activity);
            am = AccountManager.get(activity);
        }
        mSessionManager.setActivity(activity);
        return instance;
    }

    /* Get auth token by adding an account */
    public void login(final OnKeyCloakLoginListener keyCloakLoginListener) {
        if (keyCloakLoginListener == null) {
            throw new IllegalStateException("You must pass a not null OnKeyCloakLoginListener");
        }

        if (!this.hasConnected()) {

            this.am.addAccount(this.context.getString(R.string.account_type), this.context
                    .getString(R.string.account_type), null, null, this.context, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
                    try {
                        keyCloakLoginListener.onLogin(accountManagerFuture.getResult()
                                .getString(AccountManager.KEY_AUTHTOKEN));
                    }
                    /*
                        If the operation was canceled for any reason, including the user
                        canceling the creation process or adding accounts (of this type) has been disabled by policy
                     */
                    catch (OperationCanceledException e) {
                        keyCloakLoginListener.onFail(new KeyCloakConnectionResult
                                (KeyCloakConnectionResult.CANCELED));
                    }
                    /*
                        If the authenticator experienced an I/O problem creating a new account,
                        usually because of network trouble
                     */
                    catch (IOException e) {
                        keyCloakLoginListener.onFail(new KeyCloakConnectionResult
                                (KeyCloakConnectionResult.CONN_ERROR));
                    }
                    /*
                        If no authenticator was registered for this account type or the
                        authenticator failed to respond
                     */
                    catch (AuthenticatorException e) {
                        keyCloakLoginListener.onFail(new KeyCloakConnectionResult
                                (KeyCloakConnectionResult.AUTH_EXCEPTION));
                    }
                }
            }, null);

        }
    }

    public void logout(final OnKeyCloakLogoutListener onKeyCloakLogoutListener) {
        if (onKeyCloakLogoutListener == null) {
            throw new IllegalArgumentException("You must provide an OnKeyCloakLogoutListener");
        }

        onKeyCloakLogoutListener.onLogout();
    }

    private boolean hasConnected() {
        return this.am.getAccountsByType(this.context.getString(R.string.account_type)).length > 0;
    }

    private void getAuthToken() {
        Account[] accounts = this.am.getAccountsByType(this.context.getString(R.string
                .account_type));
        Account theOne = accounts[0];
        AccountManagerFuture<Bundle> authToken = this.am.getAuthToken(theOne, this
                .context.getString(R.string.account_type), null, this.context, new
                AccountManagerCallback<Bundle>() {

                    @Override
                    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
                        //keyCloakLoginListener.onLogin(accountManagerFuture.getResult()
                        //        .getString(AccountManager.KEY_AUTHTOKEN));
                    }
                }, null);
    }

    public interface OnKeyCloakLoginListener {
        void onLogin(String accessToken);

        void onFail(KeyCloakConnectionResult result);
    }

    public interface OnKeyCloakLogoutListener {
        void onLogout();
    }

}
