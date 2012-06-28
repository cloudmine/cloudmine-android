package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.rest.callbacks.*;
import com.cloudmine.api.rest.response.FileLoadResponse;
import com.cloudmine.api.rest.response.LoginResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;
import com.cloudmine.api.rest.response.SimpleCMObjectResponse;
import com.cloudmine.test.CloudMineTestRunner;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
    private int FUTURE_WAIT_TIME = 10;

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

                SimpleCMObjectResponse loadResponse = CMWebService.getService().loadObject(object.getObjectId());
                SimpleCMObject loadedObject = loadResponse.getSimpleCMObject(object.getObjectId());
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
                SimpleCMObjectResponse response = CMWebService.getService().getUserWebService(token).loadObject(object.getObjectId());
                assertTrue(response.wasSuccess());
                SimpleCMObject loadedObject = response.getSimpleCMObject(object.getObjectId());
                assertNotNull(loadedObject);
                assertEquals(object, loadedObject);
            }
        }));
        waitThenAssertTestResults();
        final CMSessionToken token = user.login().get().getSessionToken();
        SimpleCMObjectResponse response = CMWebService.getService().getUserWebService(token).loadObject(object.getObjectId());
        assertTrue(response.wasSuccess());
        assertEquals(response.getSimpleCMObject(object.getObjectId()), object);
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
                store.loadAllUserObjects(testCallback(new SimpleCMObjectResponseCallback() {
                    public void onCompletion(SimpleCMObjectResponse response) {
                        assertTrue(response.wasSuccess());
                        assertEquals("v", response.getSimpleCMObject("key").getString("k"));
                    }
                }));
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
        store.loadAllUserObjects(testCallback(new SimpleCMObjectResponseCallback() {
            public void onCompletion(SimpleCMObjectResponse response) {
                assertTrue(response.wasSuccess());
                assertEquals(userObjects.size(), response.getObjects().size());
                for (SimpleCMObject object : userObjects) {
                    SimpleCMObject responseObject = response.getSimpleCMObject(object.getObjectId());
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
        store.loadUserObjectsOfClass("testObject", hasSuccessAndHasLoaded(object));
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
    public void testCreateAndLoadUserFile() throws IOException {
        final CMUser user = user();
        final CMFile file = CMFile.CMFile(getObjectInputStream());
        file.setSaveWith(user);
        store.setUser(user);

        store.saveFile(file, hasSuccess);
        waitThenAssertTestResults();
        user.logout(hasSuccess);
        waitThenAssertTestResults();
        store.loadUserFile(file.getFileName(), testCallback(new FileLoadCallback(file.getFileName()) {
            public void onCompletion(FileLoadResponse response) {
                assertTrue(response.wasSuccess());
                assertEquals(file, response.getFile());
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
        SimpleCMObject object = simpleObject();

        store.saveObject(object, testCallback());
        waitThenAssertTestResults();
        object.add("number", 10);
        object.add("string", "name");
        object.setClass("testObject");

        store.saveStoreApplicationObjects(hasSuccessAndHasModified(object));;

        Future<SimpleCMObjectResponse> loadFuture = store.loadAllApplicationObjects();
        SimpleCMObjectResponse loadResponse = loadFuture.get(FUTURE_WAIT_TIME, TimeUnit.SECONDS);
        assertTrue(loadResponse.wasSuccess());
        SimpleCMObject loadObject = loadResponse.getSimpleCMObject(object.getObjectId());
        assertEquals(object, loadObject);
        assertEquals(object, store.getStoredObject(object.getObjectId()));
    }

    @Test
    public void testStoreFunctionOptions() throws ExecutionException, TimeoutException, InterruptedException {
        SimpleCMObject object = simpleObject();
        object.add("string", "dog");
        store.saveObject(object, hasSuccess);
        waitThenAssertTestResults();
        CMServerFunction function = CMServerFunction.CMServerFunction("NewSnippet", false);
        store.loadApplicationObjectWithObjectId(object.getObjectId(), new SimpleCMObjectResponseCallback() {
            public void onCompletion(SimpleCMObjectResponse response) {
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
        SimpleCMObjectResponse loadResponse = store.loadAllApplicationObjects(Callback.DO_NOTHING, options).get(FUTURE_WAIT_TIME, TimeUnit.SECONDS);
        assertTrue(loadResponse.wasSuccess());

        assertEquals(2, loadResponse.getObjects().size());
        assertEquals(5, loadResponse.getCount());

    }
}
