package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.rest.callbacks.CMObjectResponseCallback;
import com.cloudmine.api.rest.callbacks.FileLoadCallback;
import com.cloudmine.api.rest.callbacks.LoginResponseCallback;
import com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback;
import com.cloudmine.api.rest.options.CMPagingOptions;
import com.cloudmine.api.rest.options.CMRequestOptions;
import com.cloudmine.api.rest.options.CMServerFunction;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.FileLoadResponse;
import com.cloudmine.api.rest.response.LoginResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;
import com.cloudmine.api.rest.response.code.FileLoadCode;
import com.cloudmine.api.rest.response.code.LoginCode;
import com.cloudmine.api.rest.response.code.ObjectLoadCode;
import com.cloudmine.api.rest.response.code.ObjectModificationCode;
import com.cloudmine.test.CloudMineTestRunner;
import com.cloudmine.test.ExtendedCMUser;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.cloudmine.test.AsyncTestResultsCoordinator.reset;
import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.*;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * User: johnmccarthy
 * Date: 6/13/12, 3:51 PM
 */
@RunWith(CloudMineTestRunner.class)
public class CMStoreIntegrationTest extends ServiceTestBase {
    private CMStore store;

    @Before
    public void setUp() {
        super.setUp();
        store = CMStore.CMStore();
    }

    @Test
    public void testSaveObject() {
        final SimpleCMObject object = SimpleCMObject.SimpleCMObject();
        object.add("bool", true);


        store.saveObject(object, testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {

                CMObjectResponse loadResponse = CMWebService.getService().loadObject(object.getObjectId());
                SimpleCMObject loadedObject = (SimpleCMObject)loadResponse.getCMObject(object.getObjectId());
                assertNotNull(loadedObject);

                assertEquals(object, loadedObject);
                assertEquals(StoreIdentifier.DEFAULT, object.getSavedWith());

            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testSaveUserObject() throws ExecutionException, InterruptedException {
        final SimpleCMObject object = SimpleCMObject.SimpleCMObject();
        object.add("bool", true);
        final CMUser user = CMUser.CMUser("dfljdsfkdfskd@t.com", "t");
        CMWebService.getService().insert(user);


        object.setSaveWith(StoreIdentifier.StoreIdentifier(user));
        CMStore store = CMStore.CMStore();
        store.setUser(user);

        store.saveObject(object, testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse ignoredResponse) {
                assertTrue(ignoredResponse.wasSuccess());
                final CMSessionToken token = CMWebService.getService().login(user).getSessionToken();
                CMObjectResponse response = CMWebService.getService().getUserWebService(token).loadObject(object.getObjectId());
                assertTrue(response.wasSuccess());
                SimpleCMObject loadedObject = (SimpleCMObject)response.getCMObject(object.getObjectId());
                assertNotNull(loadedObject);
                assertEquals(object, loadedObject);
            }
        }));
        waitThenAssertTestResults();
        user.login(hasSuccess);
        waitThenAssertTestResults();
        CMSessionToken token = user.getSessionToken();
        CMObjectResponse response = CMWebService.getService().getUserWebService(token).loadObject(object.getObjectId());
        assertTrue(response.wasSuccess());
        assertEquals(response.getCMObject(object.getObjectId()), object);
    }

    @Test
    public void testUserLogin() {
        CMUser user = user();
        service.insert(user);
        CMSessionToken token = service.login(user).getSessionToken();
        service.getUserWebService(token).insert(SimpleCMObject.SimpleCMObject("key").add("k", "v").asJson());
        reset(2);
        store.login(user, testCallback(new LoginResponseCallback() {
            public void onCompletion(LoginResponse response) {
                assertTrue(response.wasSuccess());
                store.loadAllUserObjects(testCallback(new CMObjectResponseCallback() {
                    public void onCompletion(CMObjectResponse response) {
                        assertTrue(response.wasSuccess());
                        assertEquals("v", ((SimpleCMObject)response.getCMObject("key")).getString("k"));
                    }
                }));
            }
        }));
        waitThenAssertTestResults();
        store.login(CMUser.CMUser("dontexist@nowhere.net", "sp"), testCallback(new LoginResponseCallback() {
            public void onCompletion(LoginResponse response) {
                assertEquals(LoginCode.MISSING_OR_INVALID_AUTHORIZATION, response.getResponseCode());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testLoadUserObjects() {
        SimpleCMObject appObject = SimpleCMObject.SimpleCMObject();
        appObject.add("SomeKey", "Value");


        service.insert(appObject.asJson());

        final CMUser user = user();
        service.insert(user);
        final CMSessionToken token = service.login(user).getSessionToken();

        final List<SimpleCMObject> userObjects = new ArrayList<SimpleCMObject>();
        UserCMWebService userService = service.getUserWebService(token);
        for(int i = 0; i < 5; i++) {
            SimpleCMObject userObject = SimpleCMObject.SimpleCMObject();
            userObject.add("integer", i);
            userObjects.add(userObject);
            userService.insert(userObject.asJson());
        }
        store.setUser(user);
        store.loadAllUserObjects(testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
                assertEquals(userObjects.size(), response.getObjects().size());
                for (SimpleCMObject object : userObjects) {
                    SimpleCMObject responseObject = (SimpleCMObject)response.getCMObject(object.getObjectId());
                    assertEquals(StoreIdentifier.StoreIdentifier(user), responseObject.getSavedWith());
                    assertEquals(object, responseObject);
                }
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testLoadUserObjectsOfClass() {
        SimpleCMObject object = simpleObject();
        object.setClass("testObject");
        object.setSaveWith(user());
        object.save(hasSuccess);

        waitThenAssertTestResults();
        user().logout(hasSuccess);
        waitThenAssertTestResults();
        store.setUser(user());
        user().setPassword(USER_PASSWORD);
        store.loadUserObjectsOfClass("testObject", hasSuccessAndHasLoaded(object));
        waitThenAssertTestResults();

    }

    @Test
    public void testLoadUserProfilesSearch() {

        int numberOfUsers = 5;
        reset(numberOfUsers);
        final List<ExtendedCMUser> expectedLoadedUsers = new ArrayList<ExtendedCMUser>();
        for(int ageMultiplier = 0; ageMultiplier < numberOfUsers; ageMultiplier++) {
            ExtendedCMUser user = new ExtendedCMUser(randomEmail(), randomString());
            user.setAge(ageMultiplier * 10);
            user.save(testCallback());
            if(user.getAge() < 30) {
                expectedLoadedUsers.add(user);
            }
        }
        assertEquals(3, expectedLoadedUsers.size());
        waitThenAssertTestResults();

        store.loadUserProfilesSearch("[age < 30]", testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
                for(CMObject expectedUser : expectedLoadedUsers) {
                    assertEquals(expectedUser, response.getCMObject(expectedUser.getObjectId()));
                }
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testDeleteObjects() {
        final SimpleCMObject appObject = SimpleCMObject.SimpleCMObject();
        appObject.add("SomeKey", "Value");
        service.insert(appObject.asJson());

        store.deleteObject(appObject, testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                assertTrue(response.wasSuccess());
                response.wasDeleted(appObject.getObjectId());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testSaveStoreUserObjects() {
        SimpleCMObject object = simpleUserObject();
        store.addObject(object);
        store.setUser(user());
        store.saveStoreUserObjects(hasSuccessAndHasModified(object));
        waitThenAssertTestResults();
    }

    @Test
    public void testDeleteUserObject() {
        final SimpleCMObject userObject = SimpleCMObject.SimpleCMObject();
        userObject.add("key", "value");

        CMUser user = user();

        userObject.setSaveWith(user);
        store.setUser(user);
        store.saveObject(userObject, testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                assertTrue(response.wasSuccess());
                assertTrue(response.wasCreated(userObject.getObjectId()));
            }
        }));
        waitThenAssertTestResults();
        assertEquals(1, store.getStoredObjects().size());
        store.deleteObject(userObject, testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                assertTrue(response.wasSuccess());
                assertTrue(response.wasDeleted(userObject.getObjectId()));
            }
        }));
        waitThenAssertTestResults();
        assertEquals(0, store.getStoredObjects().size());
    }

    @Test
    public void testInvalidCredentialsUserOperation() {
        store.setUser(CMUser.CMUser("xnnxNOEXISTMANxnxnx@hotmail.com", "t"));
        store.loadUserObjectsOfClass("whatever", new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                assertEquals(ObjectLoadCode.MISSING_OR_INVALID_CREDENTIALS, response.getResponseCode());
            }
        });
    }

    @Test
    public void testCreateAndLoadUserFile() throws IOException {
        final CMUser user = user();
        final CMFile file = CMFile.CMFile(getObjectInputStream());
        file.setSaveWith(user);
        store.setUser(user);

        store.saveFile(file, hasSuccess);
        waitThenAssertTestResults();
        user.logout(hasSuccess);
        user.setPassword(USER_PASSWORD);
        waitThenAssertTestResults();
        store.loadUserFile(file.getFileName(), testCallback(new FileLoadCallback(file.getFileName()) {
            public void onCompletion(FileLoadResponse response) {
                assertTrue(response.wasSuccess());
                assertEquals(file, response.getFile());
            }
        }));
        waitThenAssertTestResults();
        store.loadUserFile("thisFileDoesNotevenexistMAN", testCallback(new FileLoadCallback("thisFileDoesNotevenexistMan") {
            public void onCompletion(FileLoadResponse response) {
                assertEquals(FileLoadCode.APPLICATION_ID_OR_FILE_NOT_FOUND, response.getResponseCode());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testDeleteUserFile() throws IOException {
        final CMUser user = user();
        final CMFile file = CMFile.CMFile(getObjectInputStream());

        file.setSaveWith(user);
        file.save(hasSuccess);
        waitThenAssertTestResults();
        store.setUser(user);
        store.deleteUserFile(file.getFileName(), testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                assertTrue(response.wasSuccess());
                assertTrue(response.wasDeleted(file.getFileName()));
                assertEquals(ObjectModificationCode.SUCCESS, response.getResponseCode());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testSaveAddedObjects() throws ExecutionException, TimeoutException, InterruptedException {
        SimpleCMObject appObject = SimpleCMObject.SimpleCMObject();
        appObject.setSaveWith(StoreIdentifier.DEFAULT);
        appObject.add("simple", "value");

        store.addObject(appObject);
        store.saveStoreApplicationObjects(hasSuccessAndHasModified(appObject));
        waitThenAssertTestResults();
    }

    @Test
    public void testStoreKeepValues() throws ExecutionException, TimeoutException, InterruptedException {
        final SimpleCMObject object = simpleObject();

        store.saveObject(object, testCallback());
        waitThenAssertTestResults();
        object.add("number", 10);
        object.add("string", "name");
        object.setClass("testObject");

        store.saveStoreApplicationObjects(hasSuccessAndHasModified(object));
        waitThenAssertTestResults();
        store.loadAllApplicationObjects(testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse loadResponse) {
                assertTrue(loadResponse.wasSuccess());
                CMObject loadObject = loadResponse.getCMObject(object.getObjectId());
                assertEquals(object, loadObject);
                assertEquals(object, store.getStoredObject(object.getObjectId()));
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testStoreFunctionOptions() throws ExecutionException, TimeoutException, InterruptedException {
        SimpleCMObject object = simpleObject();
        object.add("string", "dog");
        store.saveObject(object, hasSuccess);
        waitThenAssertTestResults();
        CMServerFunction function = CMServerFunction.CMServerFunction("NewSnippet", false);
        store.loadApplicationObjectWithObjectId(object.getObjectId(), new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                Object result = response.getObject("result");
                assertNotNull(result);
            }
        },
                CMRequestOptions.CMRequestOptions(function));
    }

    @Test
    public void testStorePagingOptions() throws ExecutionException, TimeoutException, InterruptedException {
        Collection<SimpleCMObject> objects = new ArrayList<SimpleCMObject>();
        for(int i = 0; i < 5; i++) {
            objects.add(simpleObject());
        }
        store.addObjects(objects);
        store.saveStoreApplicationObjects(hasSuccess);
        waitThenAssertTestResults();
        CMRequestOptions options = CMRequestOptions.CMRequestOptions(CMPagingOptions.CMPagingOptions(2, 0, true));
        store.loadAllApplicationObjects(testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse loadResponse) {
                assertTrue(loadResponse.wasSuccess());

                assertEquals(2, loadResponse.getObjects().size());
                assertEquals(5, loadResponse.getCount());
            }
        }), options);
        waitThenAssertTestResults();
    }

    @Test
    public void testLoadAllUsers() {
        final List<CMUser> users = new ArrayList<CMUser>();
        for(String userName : new String[]{"fred", "liz", "sarah"}) {
            CMUser user = CMUser.CMUser(userName + "@gmail.com", "password");
            users.add(user);
            CMWebService.getService().insert(user);
        }
        store.loadAllUserProfiles(testCallback(new CMObjectResponseCallback() {
            @Override
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
                assertTrue(response.getObjects().size() >= 3);
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testLoadLoggedInUser() {
        final CMUser user = user();
        service.insert(user);
        user.login(hasSuccess);
        waitThenAssertTestResults();
        store.setUser(user);
        store.loadLoggedInUserProfile(testCallback(new CMObjectResponseCallback() {
            @Override
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
//                assertEquals(user, response.getObjects().get(0)); //TODO once CMUsers work correctly with deserialization we can fix this
            }
        }));
        waitThenAssertTestResults();
    }
}
