package org.keycloak.keycloakaccountprovider;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import org.keycloak.keycloakaccountprovider.util.TokenExchangeUtils;

import java.util.Date;


/**
 * Created by Summers on 9/12/2014.
 */
public class KeyCloakAccountAuthenticator extends AbstractAccountAuthenticator {

    private static final String TAG = KeyCloakAccountAuthenticator.class.getCanonicalName();

    private final Context context;
    private final AccountManager am;
    private final KeyCloak kc;

    public KeyCloakAccountAuthenticator(Context context) {
        super(context);
        this.context = context.getApplicationContext();
        this.am = AccountManager.get(context);
        this.kc = new KeyCloak(context);
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "addAccount called!");

        Bundle toReturn = new Bundle();
        AccountManager am = AccountManager.get(context);

        /*
            Launch activity to account creation
         */
        if (options == null || options.getString(this.context.getString(R.string.account_key)) == null) {
            toReturn.putParcelable(AccountManager.KEY_INTENT, new Intent(context, KeycloakAuthenticationActivity.class).putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response));
            toReturn.putParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        }
        /*
            Retrieves from stored account JSON
         */
        else {
            KeyCloakAccount account = new Gson().fromJson(options.getString(this.context.getString(R.string.account_key)), KeyCloakAccount.class);
            am.removeAccount(new Account(account.getPreferredUsername(), this.context.getString(R.string.account_type)), null, null);
            am.addAccountExplicitly(new Account(account.getPreferredUsername(), this.context.getString(R.string.account_type)), null, options);
            toReturn.putString(AccountManager.KEY_ACCOUNT_NAME, account.getPreferredUsername());
            toReturn.putString(AccountManager.KEY_ACCOUNT_TYPE, this.context.getString(R.string.account_type));
        }

        return toReturn;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String authTokenType, Bundle loginOptions) throws NetworkErrorException {
        Log.d(TAG, "getAuthToken called!");

        if (!this.context.getString(R.string.account_type).equals(authTokenType)) {
            Log.i(this.getClass().getName(), this.context.getString(R.string.account_type) + "!=" + authTokenType);
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        Account[] accounts = am.getAccountsByType(this.context.getString(R.string.account_type));
        for (Account existingAccount : accounts) {
            if (existingAccount.name.equals(account.name)) {
                break;
            }
        }


        String keyCloackAccount = am.getUserData(account, this.context.getString(R.string.account_key));
        KeyCloakAccount kcAccount = new Gson().fromJson(keyCloackAccount, KeyCloakAccount.class);
        if (kcAccount == null) {
            Log.i(this.getClass().getName(), "No Account");
            Bundle toReturn = new Bundle();
            toReturn.putParcelable(AccountManager.KEY_INTENT, new Intent(context, KeycloakAuthenticationActivity.class).putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse));
            //toReturn.putParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
            return toReturn;
        }

        Log.i(this.getClass().getName(), "Expires On:" + new Date(kcAccount.getExpiresOn()));
        Log.i(this.getClass().getName(), "Refresh Expires On:" + new Date(kcAccount.getRefreshExpiresOn()));

        if (new Date(kcAccount.getExpiresOn()).before(new Date())) {
            if (new Date(kcAccount.getRefreshExpiresOn()).before(new Date())) {
                Log.i(this.getClass().getName(), "Refresh Token expired :" + kcAccount.getAccessToken());
                am.invalidateAuthToken(this.context.getString(R.string.account_type), kcAccount.getAccessToken());
                Bundle toReturn = new Bundle();
                toReturn.putParcelable(AccountManager.KEY_INTENT, new Intent(context, KeycloakAuthenticationActivity.class).putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse));
                //toReturn.putParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
                return toReturn;
            } else {
                Log.i(this.getClass().getName(), "Token expired");
                TokenExchangeUtils.refreshToken(kcAccount, kc);
                String accountJson = new Gson().toJson(kcAccount);
                am.setUserData(new Account(kcAccount.getPreferredUsername(), this.context.getString(R.string.account_type)), this.context.getString(R.string.account_key), accountJson);
            }


//            try {
//                TokenExchangeUtils.refreshToken(kcAccount, kc);
//                String accountJson = new Gson().toJson(kcAccount);
//                am.setUserData(new Account(kcAccount.getPreferredUsername(), KeyCloak.ACCOUNT_TYPE), KeyCloak.ACCOUNT_KEY, accountJson);
//            } catch (HttpException e) {
//                if (e.getStatusCode() / 100 == 4) {
//                    Bundle toReturn = new Bundle();
//                    toReturn.putParcelable(AccountManager.KEY_INTENT, new Intent(context, KeycloakAuthenticationActivity.class).putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse));
//                    toReturn.putParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
//                    return toReturn;
//                } else {
//                    Bundle toReturn = new Bundle();
//                    toReturn.putString(AccountManager.KEY_ERROR_CODE, e.getStatusCode() + "");
//                    toReturn.putString(AccountManager.KEY_ERROR_MESSAGE, e.getMessage());
//                    return toReturn;
//                }
//            }
        }

        Bundle toReturn = new Bundle();
        toReturn.putString(AccountManager.KEY_AUTHTOKEN, kcAccount.getAccessToken());
        toReturn.putString(AccountManager.KEY_ACCOUNT_NAME, kcAccount.getPreferredUsername());
        toReturn.putString(AccountManager.KEY_ACCOUNT_TYPE, this.context.getString(R.string.account_type));
        return toReturn;
    }


    @Override
    public String getAuthTokenLabel(String s) {
        return "KeyCloak Token";
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {

        String keyCloackAccount = AccountManager.get(context).getUserData(account, this.context.getString(R.string.account_key));
        KeyCloakAccount kca = new Gson().fromJson(keyCloackAccount, KeyCloakAccount.class);

        if (kca.getExpiresOn() < new Date().getTime()) {
            throw new RuntimeException("token expired");
        }

        Bundle toReturn = new Bundle();
        toReturn.putString(AccountManager.KEY_ACCOUNT_NAME, kca.getPreferredUsername());
        toReturn.putString(AccountManager.KEY_ACCOUNT_TYPE, this.context.getString(R.string.account_type));

        return toReturn;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        return null;
    }


}
