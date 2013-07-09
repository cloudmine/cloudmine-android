package com.cloudmine.api.db;

import android.content.ContentValues;
import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.LibrarySpecificClassCreator;
import com.cloudmine.api.Strings;
import com.cloudmine.api.rest.CMURLBuilder;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.cloudmine.api.db.RequestDBOpenHelper.*;


/**
 * Encapsulates all of the information needed to perform a request.
 *
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class Request {

    private static final String APP_SAVE_URL;
    private static final String USER_SAVE_URL;
    static {
        CMURLBuilder builder = new CMURLBuilder(CMApiCredentials.getApplicationIdentifier());
        APP_SAVE_URL = builder.copy().text().asUrlString();
        USER_SAVE_URL = builder.user().text().asUrlString();
    }

    public static enum Verb {
        GET("get"), PUT("put");
        private final String representation;

        private Verb(String representation) {
            this.representation = representation;
        }

        public String getRepresentation() {
            return representation;
        }

        public boolean is(String representation) {
            return getRepresentation().equalsIgnoreCase(representation);
        }

        public static Verb getVerb(String name) {
            for (Verb verb : values()) {
                if (verb.is(name)) {
                    return verb;
                }
            }
            return GET;
        }
    }

    public static enum SyncStatus {
        UNSYNCED, IN_PROGRESS, SYNCED;

        public static SyncStatus getSyncStatus(int value) {
            switch (value) {
                case 0:
                    return UNSYNCED;
                case 1:
                    return IN_PROGRESS;
                case 2:
                    return SYNCED;
                default:
                    return UNSYNCED;
            }
        }
    }

    public static Request createApplicationObjectRequest(String objectId) {
        Request request = new Request(APP_SAVE_URL, Verb.PUT, (String)null, objectId, -1, SyncStatus.UNSYNCED, new ArrayList<Header>(LibrarySpecificClassCreator.getCreator().getHeaderFactory().getCloudMineHeaders()));
        return request;
    }

    private final String requestUrl;
    private final Verb requestType;
    private String jsonBody;
    private final String objectId;
    private final int id;
    private final SyncStatus syncStatus;
    private final List<Header> headers;

    public Request(String requestUrl, Verb requestType, String jsonBody) {
        this(requestUrl, requestType, jsonBody, new ArrayList<Header>());
    }

    /**
     * Create a new request that is unsynced
     * @param requestUrl
     * @param requestType
     * @param jsonBody
     * @param headers
     */
    public Request(String requestUrl, Verb requestType, String jsonBody, List <Header> headers) {
        this(requestUrl, requestType, jsonBody, null, -1, SyncStatus.UNSYNCED, headers);
    }

    public Request(String requestUrl, Verb requestType, String jsonBody, String objectId, int id, SyncStatus syncStatus, List<Header> headers) {
        this.requestUrl = requestUrl;
        this.requestType = requestType;
        this.jsonBody = jsonBody;
        this.id = id;
        this.syncStatus = syncStatus;
        if(headers == null)
            headers = new ArrayList<Header>();
        this.headers = headers;
        this.objectId = objectId;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public Verb getRequestType() {
        return requestType;
    }

    public String getJsonBody() {
        return jsonBody;
    }

    public void setJsonBody(String jsonBody) {
        this.jsonBody = jsonBody;
    }

    public int getId() {
        return id;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void addHeader(Header toAdd) {
        headers.add(toAdd);
    }

    public String getObjectId() {
        return objectId;
    }

    public ContentValues[] toContentValues() {
        int numberOfValues = headers == null ?
                1 :
                headers.size() + 1;
        ContentValues[] values = new ContentValues[numberOfValues];
        values[0] = toRequestContentValues();
        int i = 1;
        for(ContentValues contentValues : toHeaderContentValues()) {
            values[i] = contentValues;
            i++;
        }
        return values;
    }

    public ContentValues toRequestContentValues() {
        ContentValues requestContentValues = new ContentValues();
        requestContentValues.put(KEY_REQUEST_JSON_BODY, jsonBody);
        requestContentValues.put(KEY_REQUEST_SYNCHRONIZED, syncStatus.ordinal());
        requestContentValues.put(KEY_REQUEST_TARGET_URL, requestUrl);
        requestContentValues.put(KEY_REQUEST_VERB, requestType.name());
        requestContentValues.put(KEY_REQUEST_OBJECT_ID, objectId);
        return requestContentValues;
    }

    public ContentValues[] toHeaderContentValues() {
        if(headers == null || headers.size() == 0) {
            return new ContentValues[0];
        }
        ContentValues[] headerContentValues = new ContentValues[headers.size()];
        int i = 0;
        for(Header header : headers) {
            headerContentValues[i] = headerToContentValues(header);
            i++;
        }
        return headerContentValues;
    }

    public HttpUriRequest toHttpRequest() {
        HttpUriRequest request = null;
        switch(requestType) {
            case GET:
                request = new HttpGet(requestUrl);
                break;
            case PUT:
                request = new HttpPut(requestUrl);
                try {
                    if(Strings.isNotEmpty(jsonBody)) {
                        ((HttpPut)request).setEntity(new StringEntity(jsonBody));
                        request.addHeader("Content-Type", "application/json");
                    }
                } catch (UnsupportedEncodingException e) {
                }
        }
        for(Header header : headers) {
            request.addHeader(header);
        }
        return request;
    }

    private static ContentValues headerToContentValues(Header header) {
        ContentValues headerValues = new ContentValues();
        if(header == null) {
            return headerValues;
        }
        headerValues.put(KEY_HEADER_NAME, header.getName());
        headerValues.put(KEY_HEADER_VALUE, header.getValue());
        return headerValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Request request = (Request) o;

        if (id != request.id) return false;
        if (headers != null ? !headers.equals(request.headers) : request.headers != null) return false;
        if (jsonBody != null ? !jsonBody.equals(request.jsonBody) : request.jsonBody != null) return false;
        if (objectId != null ? !objectId.equals(request.objectId) : request.objectId != null) return false;
        if (requestType != request.requestType) return false;
        if (requestUrl != null ? !requestUrl.equals(request.requestUrl) : request.requestUrl != null) return false;
        if (syncStatus != request.syncStatus) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = requestUrl != null ? requestUrl.hashCode() : 0;
        result = 31 * result + (requestType != null ? requestType.hashCode() : 0);
        result = 31 * result + (jsonBody != null ? jsonBody.hashCode() : 0);
        result = 31 * result + (objectId != null ? objectId.hashCode() : 0);
        result = 31 * result + id;
        result = 31 * result + (syncStatus != null ? syncStatus.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestUrl='" + requestUrl + '\'' +
                ", requestType=" + requestType +
                ", jsonBody='" + jsonBody + '\'' +
                ", objectId='" + objectId + '\'' +
                ", id=" + id +
                ", syncStatus=" + syncStatus +
                ", headers=" + headers +
                '}';
    }
}
