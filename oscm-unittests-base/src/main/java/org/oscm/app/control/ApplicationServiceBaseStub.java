/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.app.control;

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

/**
 * Base implementation of the local ApplicationServiceBaseStub interface only
 * implements the getServiceProfile(TechnicalProduct, String) method.
 * 
 * @author pock
 * 
 */
public class ApplicationServiceBaseStub implements ApplicationServiceLocal {

    public final static String EVENT_ID_FILE_DOWNLOAD = "FILE_DOWNLOAD";
    public final static String EVENT_ID_FILE_UPLOAD = "FILE_UPLOAD";
    public final static String EVENT_ID_FOLDER_NEW = "FOLDER_NEW";

    public final static String PARAM_MAX_FOLDER_NUM = "MAX_FOLDER_NUMBER";
    public final static String PARAM_MAX_FILE_NUM = "MAX_FILE_NUMBER";

    @Override
    public InstanceResult createInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        return null;
    }

    @Override
    public User[] createUsers(Subscription subscription,
            List<UsageLicense> usageLicenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        return new User[] {};
    }

    @Override
    public void deleteInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

    }

    @Override
    public void deleteUsers(Subscription subscription,
            List<UsageLicense> licenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

    }

    @Override
    public void validateCommunication(TechnicalProduct techProduct) {

    }

    @Override
    public void modifySubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

    }

    @Override
    public void updateUsers(Subscription subscription,
            List<UsageLicense> licenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

    }

    @Override
    public BaseResult asyncCreateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        return new BaseResult();
    }

    @Override
    public User[] createUsersForSubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        return createUsers(subscription, subscription.getUsageLicenses());
    }

    @Override
    public OperationResult executeServiceOperation(String userId,
            Subscription subscription, String transactionId,
            TechnicalProductOperation operation, Map<String, String> parameters)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        return new OperationResult();
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
