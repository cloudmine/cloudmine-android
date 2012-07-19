package com.cloudmine.api.rest;

import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.CMUser;
import com.cloudmine.api.rest.callbacks.CreationResponseCallback;
import com.cloudmine.api.rest.callbacks.LoginResponseCallback;
import com.cloudmine.api.rest.response.CreationResponse;
import com.cloudmine.api.rest.response.LoginResponse;
import com.cloudmine.api.rest.response.code.CMResponseCode;
import com.cloudmine.test.CloudMineTestRunner;
import com.cloudmine.test.ExtendedCMUser;
import com.cloudmine.test.ServiceTestBase;
import com.cloudmine.test.TestServiceCallback;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.*;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
@RunWith(CloudMineTestRunner.class)
public class CMUserIntegrationTest extends ServiceTestBase {

    @Test
    public void testLogin() throws ExecutionException, TimeoutException, InterruptedException {
        final CMUser user = CMUser.CMUser("test13131313@test.com", "test");
        service.insert(user);

        user.login(testCallback(new LoginResponseCallback() {
            public void onCompletion(LoginResponse loginResponse) {
                CMSessionToken token = loginResponse.getSessionToken();
                assertTrue(loginResponse.wasSuccess());
                assertTrue(user.isLoggedIn());
                assertFalse(CMSessionToken.INVALID_TOKEN.equals(token));
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testLogout() {
        CMUser user = user();

        user.login(hasSuccess);
        waitThenAssertTestResults();

        user.logout(hasSuccess);
        waitThenAssertTestResults();
        assertFalse(user.isLoggedIn());
    }

    @Test
    @Ignore //TODO we need to be able to delete users for this to work!
    public void testCreateUser() {
        final CMUser user = CMUser.CMUser("user45435345x345f3@user.com", "w");

        user.createUser(TestServiceCallback.testCallback(new CreationResponseCallback() {
            @Override
            public void onCompletion(CreationResponse response) {
                assertTrue(response.wasSuccess());
                assertEquals(response.getObjectId(), user.getObjectId());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testCreateUserErrors() {
        CMUser user = CMUser.CMUser("@.notanEm!l", "pw");
        user.createUser(TestServiceCallback.testCallback(new CreationResponseCallback() {
            @Override
            public void onCompletion(CreationResponse response) {
                assertEquals(CMResponseCode.INVALID_EMAIL_OR_MISSING_PASSWORD, response.getResponseCode());
            }
        }));
        waitThenAssertTestResults();
        user = CMUser.CMUser("vali@email.com", "");
        user.createUser(TestServiceCallback.testCallback(new CreationResponseCallback() {
            @Override
            public void onCompletion(CreationResponse response) {
                assertEquals(CMResponseCode.INVALID_EMAIL_OR_MISSING_PASSWORD, response.getResponseCode());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testUserProfile() {
        ExtendedCMUser user = new ExtendedCMUser("frexxd@francis.com", "pw");
        user.save(testCallback());
        waitThenAssertTestResults();
        user.login(hasSuccess);
        waitThenAssertTestResults();
        user.setAge(50);
        user.save(hasSuccess);
        waitThenAssertTestResults();

        ExtendedCMUser reloadedUser = new ExtendedCMUser("frexxd@francis.com", "pw");
        reloadedUser.login(hasSuccess);
        waitThenAssertTestResults();
        assertEquals(50, reloadedUser.getAge());
    }
}
