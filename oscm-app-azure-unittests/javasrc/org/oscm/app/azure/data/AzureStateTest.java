/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *******************************************************************************/
package org.oscm.app.azure.data;

import com.microsoft.azure.management.network.models.ProvisioningState;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by PLGrubskiM on 2017-03-27.
 */
public class AzureStateTest {

    AzureState azureState;

    @Test
    public void constructorTest() {
        // given
        final String succeeded = ProvisioningState.SUCCEEDED;
        final String deleting = ProvisioningState.DELETING;
        final String failed = ProvisioningState.FAILED;
        final String updating = ProvisioningState.UPDATING;

        final String statusCode = "Ok";
        // when
        azureState = new AzureState(succeeded);
        // then
        Assert.assertTrue(azureState.getProvisioningState().equals(succeeded));
        Assert.assertTrue(azureState.getStatusCode().isEmpty());
        Assert.assertTrue(!azureState.isDeleted());
        Assert.assertTrue(!azureState.isFailed());
        Assert.assertTrue(azureState.isSucceeded());

        // when
        azureState.setProvisioningState(deleting);
        // then
        Assert.assertTrue(azureState.getProvisioningState().equals(deleting));
        Assert.assertTrue(azureState.getStatusCode().isEmpty());
        Assert.assertTrue(azureState.isDeleted());
        Assert.assertTrue(!azureState.isFailed());
        Assert.assertTrue(!azureState.isSucceeded());

        // when
        azureState.setProvisioningState(failed);
        // then
        Assert.assertTrue(azureState.getProvisioningState().equals(failed));
        Assert.assertTrue(azureState.getStatusCode().isEmpty());
        Assert.assertTrue(!azureState.isDeleted());
        Assert.assertTrue(azureState.isFailed());
        Assert.assertTrue(!azureState.isSucceeded());

        // when
        azureState.setProvisioningState(updating);
        // then
        Assert.assertTrue(azureState.getProvisioningState().equals(updating));
        Assert.assertTrue(azureState.getStatusCode().isEmpty());
        Assert.assertTrue(!azureState.isDeleted());
        Assert.assertTrue(!azureState.isFailed());
        Assert.assertTrue(!azureState.isSucceeded());

        // when
        azureState.setStatusCode(statusCode);
        // then
        azureState.getStatusCode().equals(statusCode);
    }
}
