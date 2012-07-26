package com.cloudmine.api.rest;

import com.cloudmine.api.DeviceIdentifier;
import org.apache.http.Header;

import java.util.Set;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class AndroidHeaderFactory extends JavaHeaderFactory {
    public static final String CLOUD_MINE_AGENT = "Android/0.2";

    @Override
    public Set<Header> getCloudMineHeaders() {
        Set<Header> headers = super.getCloudMineHeaders();
        return headers;
    }

    protected String getDeviceIdentifier() {
        return DeviceIdentifier.getUniqueId();
    }

    @Override
    public String getCloudMineAgent() {
        return CLOUD_MINE_AGENT;
    }
}
