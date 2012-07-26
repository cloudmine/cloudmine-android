package com.cloudmine.api.rest;

import com.cloudmine.api.DeviceIdentifier;
import com.cloudmine.api.integration.CloudMineWebServiceIntegrationTest;
import com.cloudmine.test.AsyncTestResultsCoordinator;
import com.cloudmine.test.CloudMineTestRunner;
import com.xtremelabs.robolectric.Robolectric;
import junit.framework.Assert;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

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
    public void testSentHeaders() {
        Robolectric.getFakeHttpLayer().interceptHttpRequests(true);
        try {
            String firstResponseId = "abc123";
            BasicHeader responseHeader = new BasicHeader(HeaderFactory.REQUEST_ID_KEY, firstResponseId);
            String responseJson = "{ \"success\": { \"someKey\":\"deleted\" } }";
            Robolectric.addPendingHttpResponse(200, responseJson, responseHeader);
            service.asyncDelete("someKey", hasSuccess);
            AsyncTestResultsCoordinator.waitThenAssertTestResults();

            HttpRequest sentRequest = Robolectric.getSentHttpRequest(0);
            Header idHeader = sentRequest.getFirstHeader(HeaderFactory.DEVICE_HEADER_KEY);
            assertNotNull(idHeader);
            Assert.assertEquals(DeviceIdentifier.getUniqueId(), idHeader.getValue());

            Header agentHeader = sentRequest.getFirstHeader(HeaderFactory.AGENT_HEADER_KEY);
            assertNotNull(agentHeader);
            Assert.assertEquals(AndroidHeaderFactory.CLOUD_MINE_AGENT, agentHeader.getValue());

            responseHeader = new BasicHeader(HeaderFactory.REQUEST_ID_KEY, "def456");
            Robolectric.addPendingHttpResponse(200, responseJson, responseHeader);
            service.asyncDelete("someKey", hasSuccess);
            AsyncTestResultsCoordinator.waitThenAssertTestResults();


            HttpRequest secondRequest = Robolectric.getSentHttpRequest(1);
            assertNotNull(secondRequest);

            idHeader = secondRequest.getFirstHeader(HeaderFactory.DEVICE_HEADER_KEY);
            assertNotNull(idHeader);
            String value = idHeader.getValue();
            assertTrue(value.contains(firstResponseId));

        }finally {
            Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        }
    }
}
