package com.cloudmine.api.rest.callbacks;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.cloudmine.api.rest.response.ResponseConstructor;

/**
 * A wrapper around Callback that makes it work with the AsyncHttp library. Should not
 * be extended.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public final class AndroidCallback<T> extends AsyncHttpResponseHandler<T> implements Callback<T> {

    private final Callback callback;


    public AndroidCallback(ResponseConstructor<T> constructor) {
        this(CMCallback.doNothing(), constructor);
    }

    public AndroidCallback(Callback callback, ResponseConstructor<T> constructor) {
        super(constructor);
        this.callback = callback;

    }

    @Override
    public void onCompletion(T response) {
        callback.onCompletion(response);
    }

    @Override
    public void onFailure(Throwable error, String message) {
        callback.onFailure(error, message);
    }

    @Override
    public void setStartTime(long startTime) {
        callback.setStartTime(startTime);
    }

    @Override
    public long getStartTime() {
        return callback.getStartTime();
    }
}
