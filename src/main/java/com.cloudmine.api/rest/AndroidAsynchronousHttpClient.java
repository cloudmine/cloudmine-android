package com.cloudmine.api.rest;

import com.cloudmine.api.rest.callbacks.AndroidCallback;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.response.ResponseConstructor;
import com.loopj.android.http.AsyncHttpClient;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Implementation of AsynchronousHttpClient for the Android platform.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class AndroidAsynchronousHttpClient extends AsyncHttpClient implements AsynchronousHttpClient{
    @Override
    public <T> void executeCommand(HttpUriRequest command, Callback<T> callback, ResponseConstructor<T> constructor) {
        execute(command, new AndroidCallback(callback, constructor));
    }
}
