package com.cloudmine.api;

import android.app.Activity;
import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import com.cloudmine.api.gui.AuthenticationDialog;
import com.cloudmine.api.rest.CMSocial;
import com.cloudmine.api.rest.callbacks.CMCallback;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.callbacks.LoginResponseCallback;
import com.cloudmine.api.rest.response.CMSocialLoginResponse;
import com.cloudmine.api.rest.response.LoginResponse;

import java.util.HashMap;

/**
 * Social that requires an activity to authorize, so that the user can log in through a GUI
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMAndroidSocial extends CMSocial {

    /**
     * See {@link #loginThroughSocial(com.cloudmine.api.rest.CMSocial.Service, android.app.Activity, com.cloudmine.api.rest.callbacks.Callback)}
     * @param service
     * @param activity
     */
    public static void loginThroughSocial(final Service service, Activity activity) {
        loginThroughSocial(service, activity, CMCallback.<CMSocialLoginResponse>doNothing());
    }

    /**
     * See {@link #loginThroughSocial(com.cloudmine.api.rest.CMSocial.Service, android.app.Activity, com.cloudmine.api.rest.callbacks.Callback)}
     * @param service
     * @param activity
     * @param params
     */
    public static void loginThroughSocial(final Service service, Activity activity, HashMap<String, Object> params) {
        loginThroughSocial(service, activity, params, CMCallback.<CMSocialLoginResponse>doNothing());
    }

    /**
     * See {@link #loginThroughSocial(com.cloudmine.api.rest.CMSocial.Service, android.app.Activity, com.cloudmine.api.rest.callbacks.Callback)}
     * @param service
     * @param activity
     * @param callback
     */
    public static void loginThroughSocial(final Service service, Activity activity, final Callback<CMSocialLoginResponse> callback) {
        loginThroughSocial(service, activity, null, callback);
    }

    /**
     * Switch from the current Activity to the log in page for the given service. On completion, the current Activity
     * will be shown. If the user has already logged into this application with the specified service, their user profile
     * and a session token is passed into the given callback. If they have not, a new user is created. The newly created
     * user and their session token will be passed into the given callback
     * @param service
     * @param activity
     * @param params paramters to pass into the authentication call, such as scope.
     * @param callback expects a CMSocialLoginResponse
     */
    public static void loginThroughSocial(final Service service, Activity activity, HashMap<String, Object> params, final Callback<CMSocialLoginResponse> callback) {
        try {
            AuthenticationDialog dialog = new AuthenticationDialog(activity, service, params, callback);
            dialog.show();
        }catch(Exception e) {
            callback.onFailure(e, "");
        }
    }



    /**
     * Clear the session cookies. This is necessary if the user wishes to log in as a different user through a previously
     * authed social account. Clears all the session cookies, even if they are not social related
     * @param context
     */
    public static void clearSessionCookies(Context context) {
        CookieSyncManager syncManager = CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();
        syncManager.sync();
    }

    public static void linkToUser(final Service service, final Activity activity, final CMSessionToken sessionToken, final Callback<CMSocialLoginResponse> callback) {
        linkToUser(service, activity, sessionToken, null, callback);
    }

    public static void linkToUser(final Service service, final Activity activity, final CMSessionToken sessionToken, HashMap<String, Object> params, final Callback<CMSocialLoginResponse> callback) {
        AuthenticationDialog dialog = new AuthenticationDialog(activity, service, sessionToken, params, callback);
        dialog.show();
    }

    /**
     * See {@link #linkToUser(com.cloudmine.api.rest.CMSocial.Service, android.app.Activity, com.cloudmine.api.CMUser, java.util.HashMap, com.cloudmine.api.rest.callbacks.Callback)}
     * @param service
     * @param activity
     * @param user
     * @param callback
     */
    public static void linkToUser(final Service service, final Activity activity, final CMUser user, final Callback<CMSocialLoginResponse> callback) {
        linkToUser(service, activity, user, null, callback);
    }

    /**
     * Link a social account to the given user. If the given user is not logged in, this first attempts to log them in.
     * Requires that the given user exists and that no user has already been linked to the given service
     * @param service
     * @param activity
     * @param user
     * @param params The Parameters for the auth call, such as scope.
     * @param callback
     */
    public static void linkToUser(final Service service, final Activity activity, final CMUser user, HashMap<String, Object> params, final Callback<CMSocialLoginResponse> callback) {
        CMSessionToken sessionToken = user.getSessionToken();
        if(sessionToken.isValid())
            linkToUser(service, activity, sessionToken, callback);
        else
            user.login(new LoginResponseCallback() {
                public void onCompletion(LoginResponse response) {
                    if(response.wasSuccess())
                        linkToUser(service, activity, response.getSessionToken(), callback);
                    else
                        callback.onFailure(new IllegalStateException("Couldn't log in to CloudMine to link to user"), "LogIn failed");
                }

                public void onFailure(Throwable t, String msg) {
                    callback.onFailure(t, msg);
                }
            });
    }
}
