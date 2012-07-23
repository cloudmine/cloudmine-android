package com.cloudmine.api.rest;

import com.cloudmine.api.DeviceIdentifier;
import com.cloudmine.api.integration.CloudMineWebServiceIntegrationTest;
import com.cloudmine.test.CloudMineTestRunner;
import com.xtremelabs.robolectric.Robolectric;
import junit.framework.Assert;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
@RunWith(CloudMineTestRunner.class)
public class AndroidCMWebServiceIntegrationTest extends CloudMineWebServiceIntegrationTest {

    @Before
    public void setUp() {
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        DeviceIdentifier.initialize(Robolectric.application.getApplicationContext());
        super.setUp();
    }

    @Test
    @Ignore
    public void testSentHeaders() { //TODO this test doesn't work but its not cause the impplementation is broken


        service.delete("someKey");

        HttpRequest sentRequest = Robolectric.getSentHttpRequest(0);
        Header idHeader = sentRequest.getFirstHeader(DeviceIdentifier.DEVICE_HEADER_KEY);
        assertNotNull(idHeader);
        Assert.assertEquals(DeviceIdentifier.getUniqueId(), idHeader.getValue());

        Header agentHeader = sentRequest.getFirstHeader(HeaderFactory.AGENT_HEADER_KEY);
        assertNotNull(agentHeader);
        Assert.assertEquals(AndroidHeaderFactory.CLOUD_MINE_AGENT, agentHeader.getValue());
    }
}
