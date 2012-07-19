package com.cloudmine.api.rest;

import com.cloudmine.test.CloudMineTestRunner;
import com.cloudmine.test.ExtendedCMObject;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
@RunWith(CloudMineTestRunner.class)
public class CMObjectIntegrationTest extends ServiceTestBase{

    @Test
    public void testSave() {
        ExtendedCMObject object = new ExtendedCMObject();
        object.save(hasSuccessAndHasModified(object));
        waitThenAssertTestResults();

        service.asyncLoadObject(object.getObjectId(), hasSuccessAndHasLoaded(object));
        waitThenAssertTestResults();
    }
}
