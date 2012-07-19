package com.cloudmine.api.rest;

import com.cloudmine.api.DeviceIdentifier;
import org.apache.http.Header;

import java.util.Set;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class AndroidHeaderFactory extends HeaderFactoryStandardImpl {
    public static final String CLOUD_MINE_AGENT = "Android 1.0";

    @Override
    public Set<Header> getCloudMineHeaders() {
        Set<Header> headers = super.getCloudMineHeaders();
        headers.add(DeviceIdentifier.getDeviceIdentifierHeader());
        return headers;
    }

    @Override
    public String getCloudMineAgent() {
        return CLOUD_MINE_AGENT;
    }
}
