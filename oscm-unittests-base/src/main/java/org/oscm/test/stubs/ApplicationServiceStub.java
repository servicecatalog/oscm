/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.List;
import java.util.Map;

import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.UsageLicense;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.operation.data.OperationResult;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.User;

public class ApplicationServiceStub implements ApplicationServiceLocal {

    @Override
    public BaseResult asyncCreateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InstanceResult createInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public User[] createUsers(Subscription subscription,
            List<UsageLicense> usageLicenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public User[] createUsersForSubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteUsers(Subscription subscription,
            List<UsageLicense> licenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validateCommunication(TechnicalProduct techProduct)
            throws TechnicalServiceNotAliveException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void modifySubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateUsers(Subscription subscription,
            List<UsageLicense> licenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public OperationResult executeServiceOperation(String userId,
            Subscription subscription, String transactionId,
            TechnicalProductOperation operation, Map<String, String> parameters)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void activateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deactivateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void asyncModifySubscription(Subscription subscription,
            Product product) throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void asyncUpgradeSubscription(Subscription subscription,
            Product product) throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void upgradeSubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, List<String>> getOperationParameterValues(String userId,
            TechnicalProductOperation operation, Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAttributes(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

}
