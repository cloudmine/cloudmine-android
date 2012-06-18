/*
    Android Asynchronous Http Client
    Copyright (c) 2011 James Smith <james@loopj.com>
    http://loopj.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    ****This file has been modified from its original version by CloudMine LLC*****
*/

package com.loopj.android.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.Callable;

class AsyncHttpRequest<T> implements Callable<T> {
    private final AbstractHttpClient client;
    private final HttpContext context;
    private final HttpUriRequest request;
    private final AsyncHttpResponseHandler<T> responseHandler;
    private int executionCount;

    public AsyncHttpRequest(AbstractHttpClient client, HttpContext context, HttpUriRequest request, AsyncHttpResponseHandler<T> responseHandler) {
        this.client = client;
        this.context = context;
        this.request = request;
        this.responseHandler = responseHandler;
    }

    public void run() {
        try {
            if(responseHandler != null){
                responseHandler.sendStartMessage();
            }

            makeRequestWithRetries();

            if(responseHandler != null) {
                responseHandler.sendFinishMessage();
            }
        } catch (IOException e) {
            if(responseHandler != null) {
                responseHandler.sendFinishMessage();
                responseHandler.sendFailureMessage(e, null);
            }
        }
    }
    
    private T makeRequest() throws IOException {
        T fromRequest = null;
    	if(!Thread.currentThread().isInterrupted()) {
    		HttpResponse response = client.execute(request, context);
    		if(!Thread.currentThread().isInterrupted()) {
    			if(responseHandler != null) {
    				fromRequest = responseHandler.sendResponseMessage(response);
    			}
    		} else{
    			//TODO: should raise InterruptedException? this block is reached whenever the request is cancelled before its response is received
    		}
    	}
        return fromRequest;
    }

    private T makeRequestWithRetries() throws ConnectException {
        // This is an additional layer of retry logic lifted from droid-fu
        // See: https://github.com/kaeppler/droid-fu/blob/master/src/main/java/com/github/droidfu/http/BetterHttpRequestBase.java
        boolean retry = true;
        IOException cause = null;
        HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();
        while (retry) {
            try {
                T requestResponse = makeRequest();
                if(requestResponse != null) {
                    return requestResponse;
                }
            } catch (IOException e) {
                cause = e;
                retry = retryHandler.retryRequest(cause, ++executionCount, context);
            } catch (NullPointerException e) {
                // there's a bug in HttpClient 4.0.x that on some occasions causes
                // DefaultRequestExecutor to throw an NPE, see
                // http://code.google.com/p/android/issues/detail?id=5255
                cause = new IOException("NPE in HttpClient" + e.getMessage());
                retry = retryHandler.retryRequest(cause, ++executionCount, context);
            } 
        }

        // no retries left, crap out with exception
        ConnectException ex = new ConnectException();
        ex.initCause(cause);
        throw ex;
    }

    @Override
    public T call() throws Exception {
        T response = null;
        try {
            if(responseHandler != null){
                responseHandler.sendStartMessage();
            }

            response = makeRequestWithRetries();

            if(responseHandler != null) {
                responseHandler.sendFinishMessage();
            }
        } catch (IOException e) {
            if(responseHandler != null) {
                responseHandler.sendFinishMessage();
                responseHandler.sendFailureMessage(e, null);
            }
        }
        return response;
    }
}