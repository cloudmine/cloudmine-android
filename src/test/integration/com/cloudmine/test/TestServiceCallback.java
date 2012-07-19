package com.cloudmine.test;

import com.cloudmine.api.rest.callbacks.CMCallback;
import com.cloudmine.api.rest.callbacks.CMResponseCallback;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * CMUser: johnmccarthy
 * Date: 6/6/12, 3:36 PM
 */
public class TestServiceCallback<T> extends CMCallback<T> {

    private CMCallback<T> callback;

    public static <T> TestServiceCallback<T> testCallback(CMCallback<T> callback) {
        return new TestServiceCallback<T>(callback);
    }

    public static TestServiceCallback testCallback() {
        return new TestServiceCallback(new CMResponseCallback());
    }

    public TestServiceCallback(CMCallback<T> callback) {
        super(callback.constructor());
        this.callback = callback;
    }

    @Override
    public void onCompletion(T response) {
        try {
            callback.onCompletion(response);
        } catch(AssertionError t) {
            AsyncTestResultsCoordinator.add(t);
        } catch(Exception e){
            AsyncTestResultsCoordinator.add(new AssertionError(e));
            e.printStackTrace();
        }
        finally {
            AsyncTestResultsCoordinator.done();
        }
    }

    @Override
    public void onFailure(Throwable thrown, String message) {
        try {
            callback.onFailure(thrown, message);
        } catch(AssertionError t) {
            AsyncTestResultsCoordinator.add(t);
        }
    }
}
