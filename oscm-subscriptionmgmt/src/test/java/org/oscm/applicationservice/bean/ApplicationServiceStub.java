/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.applicationservice.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.UsageLicense;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.operation.data.OperationResult;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceInfo;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.User;

@Stateless
@Local(ApplicationServiceLocal.class)
public class ApplicationServiceStub implements ApplicationServiceLocal {

    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    public boolean userModificationCallReceived = false;

    public boolean throwProductOperationFailed = false;

    public boolean throwSaaSApplicationException = false;

    public boolean throwSaaSApplicationExceptionWithParam = false;

    public boolean throwTechnicalServiceNotAliveExceptionCustomer = false;

    public Map<String, PlatformUser> addedUsers = new HashMap<>();

    public List<PlatformUser> deletedUsers = new ArrayList<>();

    public boolean isProductDeleted = false;

    public boolean deactivated;

    public boolean activated;

    public void resetController() {
        userModificationCallReceived = false;
        throwProductOperationFailed = false;
        throwSaaSApplicationException = false;
        throwSaaSApplicationExceptionWithParam = false;
        throwTechnicalServiceNotAliveExceptionCustomer = false;
        addedUsers.clear();
        deletedUsers.clear();
        isProductDeleted = false;
    }

    @Override
    public User[] createUsers(Subscription subscription,
            List<UsageLicense> usageLicenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        User[] users = new User[usageLicenses.size()];
        List<User> userList = new ArrayList<>();
        for (UsageLicense usageLicense : usageLicenses) {
            PlatformUser platformUser = usageLicense.getUser();
            addedUsers.put(platformUser.getUserId(), platformUser);
            User user = new User();
            user.setApplicationUserId(platformUser.getUserId());
            user.setEmail(platformUser.getEmail());
            user.setLocale(platformUser.getLocale());
            user.setUserId(platformUser.getUserId());
            user.setUserFirstName(platformUser.getFirstName());
            user.setUserLastName(platformUser.getLastName());
            RoleDefinition roleDefinition = usageLicense.getRoleDefinition();
            if (roleDefinition != null) {
                user.setRoleIdentifier(roleDefinition.getRoleId());
            }
            userList.add(user);
        }
        users = userList.toArray(new User[userList.size()]);
        userModificationCallReceived = true;
        return users;
    }

    @Override
    public void deleteUsers(Subscription subscription,
            List<UsageLicense> licenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        for (UsageLicense usageLicense : licenses) {
            deletedUsers.add(usageLicense.getUser());
        }
        userModificationCallReceived = true;
    }

    @Override
    public InstanceResult createInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        InstanceResult result = new InstanceResult();
        InstanceInfo info = new InstanceInfo();
        info.setInstanceId(subscription.getSubscriptionId());
        result.setInstance(info);
        return result;
    }

    @Override
    public void deleteInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        if (throwProductOperationFailed) {
            throw new TechnicalServiceOperationException("");
        }
        isProductDeleted = true;
    }

    @Override
    public void validateCommunication(TechnicalProduct techProduct) {

    }

    @Override
    public void modifySubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        handleModifySubscription();
    }

    @Override
    public void updateUsers(Subscription subscription,
            List<UsageLicense> licenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        userModificationCallReceived = true;
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
        List<UsageLicense> licenses = subscription.getUsageLicenses();
        return createUsers(subscription, licenses);
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
        activated = true;
    }

    @Override
    public void deactivateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        deactivated = true;
    }

    @Override
    public void asyncModifySubscription(Subscription subscription,
            Product product) throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        handleModifySubscription();
    }

    @Override
    public void asyncUpgradeSubscription(Subscription subscription,
            Product product) throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        handleModifySubscription();
    }

    @Override
    public void upgradeSubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        handleModifySubscription();
    }

    private void handleModifySubscription()
            throws TechnicalServiceOperationException,
            TechnicalServiceNotAliveException {
        if (throwSaaSApplicationException) {
            if (throwSaaSApplicationExceptionWithParam) {
                throw new TechnicalServiceOperationException("testMessage",
                        new Object[] { "testparam1", "testParam2" });
            } else {
                throw new TechnicalServiceOperationException("testMessage");
            }
        }
        throwSaaSApplicationException = false;
        throwSaaSApplicationExceptionWithParam = false;
        if (throwTechnicalServiceNotAliveExceptionCustomer) {
            throw new TechnicalServiceNotAliveException(
                    TechnicalServiceNotAliveException.Reason.CUSTOMER);
        }
        throwTechnicalServiceNotAliveExceptionCustomer = false;
    }

    @Override
    public Map<String, List<String>> getOperationParameterValues(String userId,
            TechnicalProductOperation operation, Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        return new HashMap<>();
    }

    @Override
    public void saveAttributes(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
    }

}
