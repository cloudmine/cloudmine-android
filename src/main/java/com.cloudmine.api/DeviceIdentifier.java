package com.cloudmine.api;

import android.content.Context;
import android.content.SharedPreferences;
import com.cloudmine.api.rest.AndroidAsynchronousHttpClient;
import com.cloudmine.api.rest.AndroidBase64Encoder;
import com.cloudmine.api.rest.AndroidHeaderFactory;
import com.cloudmine.api.rest.HeaderFactory;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.UUID;

/**
 * An identifier that is unique per device per application. Uses SharedPreferences, so if the phone is
 * factory reset, a new identifier will be generated. Must be initialized before any calls to the
 * CloudMine api are made
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class DeviceIdentifier {

    public static final String UNIQUE_ID_KEY = "uniqueId";

    private static String uniqueId;


    /**
     * Retrieves the unique id for this application and device from the preferences. If this is the
     * first time the application has been run, a new unique id will be generated and saved in the
     * preferences
     * @param context the application context, accessable from an activity this.getApplicationContext()
     */
    public static void initialize(Context context) {
        //Not related to BaseDeviceIdentifier but we need to do the DI somewhere and this is as good as any
        LibrarySpecificClassCreator.setCreator(new LibrarySpecificClassCreator(new AndroidBase64Encoder(), new AndroidHeaderFactory(), new AndroidAsynchronousHttpClient()));

        SharedPreferences preferences = context.getSharedPreferences("CLOUDMINE_PREFERENCES", Context.MODE_PRIVATE);
        boolean isNotSet = !preferences.contains(UNIQUE_ID_KEY);
        if(isNotSet) {
            String uniqueId = generateUniqueDeviceIdentifier();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(UNIQUE_ID_KEY, uniqueId);
            editor.commit();
        }
        uniqueId = preferences.getString(UNIQUE_ID_KEY, null); //null here so if we aren't getting the unique key, we fail hard
        if(uniqueId == null) {
            throw new RuntimeException("Unable to get unique id");
        }
    }

    /**
     * Get the unique identifier for this Device and application
     * @return the unique identifier
     * @throws RuntimeException if initialize has not been called
     */
    public static String getUniqueId() throws RuntimeException {
        if(uniqueId == null) {
            throw new RuntimeException("You must call BaseDeviceIdentifier.initialize before using the cloudmine api");
        }
        return uniqueId;
    }

    /**
     * Get the header that should be included with any requests to cloudmine
     * @return the header that should be included with any requests to cloudmine
     * @throws RuntimeException if initialize has not been called
     */
    public static Header getDeviceIdentifierHeader() throws RuntimeException {
        return new BasicHeader(HeaderFactory.DEVICE_HEADER_KEY, getUniqueId());
    }

    private static String generateUniqueDeviceIdentifier() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
