package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.rest.callbacks.CMObjectResponseCallback;
import com.cloudmine.api.rest.callbacks.CreationResponseCallback;
import com.cloudmine.api.rest.options.CMRequestOptions;
import com.cloudmine.api.rest.options.CMSharedDataOptions;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.CreationResponse;
import com.cloudmine.test.CloudMineTestRunner;
import com.cloudmine.test.ServiceTestBase;
import com.cloudmine.test.TestServiceCallback;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
@RunWith(CloudMineTestRunner.class)
public class CMAccessListIntegrationTest extends ServiceTestBase {

    @Test
    public void testStoreAccessList() {
        final CMUser anotherUser = randomUser();
        anotherUser.save(hasSuccess);
        waitThenAssertTestResults();

        final CMUser user = user();
        user.login(hasSuccess);
        waitThenAssertTestResults();

        final CMAccessList list = CMAccessList.CMAccessList(user);
        List<String> userObjectIds = Arrays.asList("freddy", "teddy", "george", "puddin");
        list.grantAccessTo(userObjectIds);
        list.grantAccessTo(anotherUser);
        list.grantPermissions(CMAccessPermission.READ);
        list.save(TestServiceCallback.testCallback(new CreationResponseCallback() {
            @Override
            public void onCompletion(CreationResponse response) {
                assertTrue(response.wasSuccess());
//                assertEquals(list.getObjectId(), response.getObjectId());
            }
        }));
        waitThenAssertTestResults();


        final SimpleCMObject anObject = SimpleCMObject.SimpleCMObject();
        anObject.add("aSecret", true);
        anObject.grantAccess(list);
        anObject.saveWithUser(user, hasSuccessAndHasModified(anObject));
        waitThenAssertTestResults();

        anotherUser.login(hasSuccess);
        waitThenAssertTestResults();
        CMSessionToken token = anotherUser.getSessionToken();

        service.getUserWebService(token).asyncLoadObject(anObject.getObjectId(), TestServiceCallback.testCallback(new CMObjectResponseCallback() {
            @Override
            public void onCompletion(CMObjectResponse response) {
                anotherUser.getObjectId();
                list.getObjectId();
                assertTrue(response.hasSuccess());
                assertEquals(1, response.getObjects().size());
                CMObject loadedObject = response.getCMObject(anObject.getObjectId());
                assertEquals(anObject, loadedObject);
            }
        }), CMRequestOptions.CMRequestOptions(CMSharedDataOptions.SHARED_OPTIONS));
        waitThenAssertTestResults();
    }

    @Test
    public void testGetAccessList() {
        CMUser user = user();
        final CMAccessList list = CMAccessList.CMAccessList(user, CMAccessPermission.CREATE);
        list.grantAccessTo("whatever");
        list.save(hasSuccess);
        waitThenAssertTestResults();

        user.loadAccessLists(TestServiceCallback.testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
                CMObject loadedList = response.getCMObject(list.getObjectId());
                assertEquals(list, loadedList);
            }
        }));
        waitThenAssertTestResults();
    }
}
