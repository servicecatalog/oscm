/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-02-25                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.v2_0.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.business.exceptions.BESNotificationException;
import org.oscm.app.business.exceptions.ServiceInstanceException;
import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.dao.BesDAO;
import org.oscm.app.dao.OperationDAO;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.InstanceOperation;
import org.oscm.app.domain.Operation;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.i18n.Messages;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.service.APPTimerServiceBean;
import org.oscm.app.v2_0.service.OperationServiceBean;
import org.oscm.app.v2_0.service.ServiceInstanceServiceBean;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.types.enumtypes.OperationStatus;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;

/**
 * Unit test for ServiceInstanceServiceBean.
 * 
 * @author goebel
 */
public class ServiceInstanceServiceBeanTest {

    private static final String ALLOWED_STATUS_ABORT_COMPLETE = ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION
            .name()
            + ", "
            + ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION.name()
            + ", "
            + ProvisioningStatus.WAITING_FOR_SYSTEM_UPGRADE.name();

    private static final String ERROR_INSTANCE_STATUS_MSG_KEY = "error_instance_status_wrong";
    private static final String USER_LOCALE = "ja";

    private ServiceInstanceServiceBean bean;
    private ServiceUser user;

    @Before
    public void setup() {
        bean = new ServiceInstanceServiceBean();
        bean.dao = mock(ServiceInstanceDAO.class);
        bean.besDao = mock(BesDAO.class);
        bean.em = mock(EntityManager.class);
        bean.timerServcie = mock(APPTimerServiceBean.class);
        bean.opBean = mock(OperationServiceBean.class);
        bean.opDao = mock(OperationDAO.class);
        user = new ServiceUser();
    }

    @Test(expected = ServiceInstanceException.class)
    public void executeOperation_Failure() throws Exception {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setRunWithTimer(false);
        doReturn(instance).when(bean.em).getReference(
                eq(ServiceInstance.class), any(ServiceInstance.class));
        doThrow(new ServiceInstanceNotFoundException("")).when(bean.dao)
                .resumeInstance(instance);

        // when
        bean.executeOperation(instance, user, InstanceOperation.RESUME);
    }

    @Test
    public void getInstancesForController() throws Exception {
        // given
        String controllerId = "ROR123";

        // when
        bean.getInstancesForController(controllerId);

        // then
        verify(bean.dao).getInstancesForController(controllerId);
    }

    @Test
    public void listOperationsForInstance() {
        // given
        ServiceInstance instance = givenServiceInstance();

        // when
        bean.listOperationsForInstance(instance);
    }

    @Test(expected = ServiceInstanceException.class)
    public void executeOperation_InstanceIsNull()
            throws ServiceInstanceException {
        // when
        bean.executeOperation(null, user, InstanceOperation.RESUME);
    }

    @Test
    public void executeOperation_Delete_WithoutException_successful()
            throws Exception {
        // given
        ServiceInstance instance = givenServiceInstance();
        ServiceInstance newInstance = givenPreparedInstanceForDeletion(instance);
        doReturn(instance).when(bean.dao).getInstanceById(anyString());
        doReturn(newInstance).when(bean.dao).markAsDeleted(
                any(ServiceInstance.class));
        doReturn(instance).when(bean.em).getReference(
                eq(ServiceInstance.class), any(ServiceInstance.class));
        doNothing().when(bean.besDao).terminateSubscription(
                any(ServiceInstance.class), anyString());

        // when
        bean.executeOperation(instance, user, InstanceOperation.DELETE);

        // then
        verify(bean.dao, times(1)).markAsDeleted(eq(instance));

        verify(bean.besDao, times(1)).terminateSubscription(eq(newInstance),
                anyString());
        verify(bean.dao, times(1)).deleteInstance(eq(newInstance));
        verify(bean.dao, times(0)).restoreInstance(eq(newInstance));
    }

    @Test
    public void abortPendingInstance_ErrorInstanceStatus()
            throws ServiceInstanceNotFoundException {

        // given
        ServiceInstance instance = givenServiceInstance();
        ServiceInstance dbInstance = new ServiceInstance();
        dbInstance.setProvisioningStatus(ProvisioningStatus.COMPLETED);
        doReturn(dbInstance).when(bean.dao).find(any(ServiceInstance.class));
        doReturn(dbInstance).when(bean.dao).abortPendingInstance(
                any(ServiceInstance.class));

        // when
        try {
            bean.abortPendingInstance(instance, USER_LOCALE);
        } catch (ServiceInstanceException e) {
            // then
            assertEquals(Messages.get(USER_LOCALE,
                    ERROR_INSTANCE_STATUS_MSG_KEY,
                    ALLOWED_STATUS_ABORT_COMPLETE), e.getMessage());
        }
    }

    @Test
    public void completePendingInstance_ErrorInstanceStatus()
            throws ServiceInstanceNotFoundException {

        // given
        ServiceInstance instance = givenServiceInstance();
        ServiceInstance dbInstance = new ServiceInstance();
        dbInstance.setProvisioningStatus(ProvisioningStatus.COMPLETED);
        dbInstance.setControllerReady(true);
        doReturn(dbInstance).when(bean.dao).find(any(ServiceInstance.class));

        // when
        try {
            bean.completePendingInstance(instance, USER_LOCALE);
        } catch (ServiceInstanceException e) {
            assertEquals(Messages.get(USER_LOCALE,
                    ERROR_INSTANCE_STATUS_MSG_KEY,
                    ALLOWED_STATUS_ABORT_COMPLETE), e.getMessage());
        }
    }

    @Test
    public void executeOperation_Delete_WithException_successful()
            throws Exception {
        // given
        ServiceInstance instance = givenServiceInstance();
        ServiceInstance newInstance = givenPreparedInstanceForDeletion(instance);
        doReturn(instance).when(bean.dao).getInstanceById(anyString());
        doReturn(newInstance).when(bean.dao).markAsDeleted(
                any(ServiceInstance.class));
        doReturn(instance).when(bean.em).getReference(
                eq(ServiceInstance.class), any(ServiceInstance.class));
        doThrow(
                new BESNotificationException("message",
                        new ObjectNotFoundException("Obj not found!"))).when(
                bean.besDao).terminateSubscription(any(ServiceInstance.class),
                anyString());

        // when
        bean.executeOperation(instance, user, InstanceOperation.DELETE);

        // then
        verify(bean.dao, times(1)).markAsDeleted(eq(instance));

        verify(bean.besDao, times(1)).terminateSubscription(eq(newInstance),
                anyString());
        verify(bean.dao, times(1)).deleteInstance(eq(newInstance));
        verify(bean.dao, times(0)).restoreInstance(eq(newInstance));
    }

    @Test
    public void executeOperation_Delete_WithException_unsuccessful()
            throws Exception {
        // given
        ServiceInstance instance = givenServiceInstance();
        ServiceInstance newInstance = givenPreparedInstanceForDeletion(instance);
        doReturn(instance).when(bean.dao).getInstanceById(anyString());
        doReturn(newInstance).when(bean.dao).markAsDeleted(
                any(ServiceInstance.class));
        doReturn(instance).when(bean.em).getReference(
                eq(ServiceInstance.class), any(ServiceInstance.class));
        doThrow(
                new BESNotificationException("message",
                        new OperationNotPermittedException("No Permition!")))
                .when(bean.besDao).terminateSubscription(
                        any(ServiceInstance.class), anyString());

        try {
            bean.executeOperation(instance, user, InstanceOperation.DELETE);
        } catch (Exception e) {
            // doNothing
        }
        // then
        verify(bean.dao, times(1)).markAsDeleted(eq(instance));

        verify(bean.besDao, times(1)).terminateSubscription(eq(newInstance),
                anyString());
        verify(bean.dao, times(0)).deleteInstance(eq(newInstance));
        verify(bean.dao, times(1)).restoreInstance(eq(newInstance));
    }

    private ServiceInstance givenPreparedInstanceForDeletion(
            ServiceInstance instance) {
        ServiceInstance newInstance = instance;
        newInstance.setInstanceId(instance.getInstanceId() + "#00000000");
        return newInstance;
    }

    @Test
    public void executeOperation_Resume() throws Exception {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setRunWithTimer(false);
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        doReturn(instance).when(bean.dao).getInstanceById(anyString());
        doReturn(instance).when(bean.dao).resumeInstance(
                any(ServiceInstance.class));
        doReturn(instance).when(bean.em).getReference(
                eq(ServiceInstance.class), any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user, InstanceOperation.RESUME);

        // then
        verify(bean.timerServcie, times(1)).initTimers();
        verify(bean.dao, times(1)).resumeInstance(eq(instance));
        verify(bean.besDao, times(0)).notifyAsyncOperationStatus(
                any(ServiceInstance.class), anyString(),
                eq(OperationStatus.RUNNING), anyListOf(LocalizedText.class));
    }

    @Test
    public void executeOperation_Resume_WatingForSystemOperation()
            throws Exception {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setRunWithTimer(false);
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_OPERATION);
        doReturn(instance).when(bean.dao).getInstanceById(anyString());
        doReturn(instance).when(bean.dao).resumeInstance(
                any(ServiceInstance.class));
        doReturn(givenOperation()).when(bean.opDao).getOperationByInstanceId(
                anyString());
        doReturn(instance).when(bean.em).getReference(
                eq(ServiceInstance.class), any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user, InstanceOperation.RESUME);

        // then
        verify(bean.timerServcie, times(1)).initTimers();
        verify(bean.dao, times(1)).resumeInstance(eq(instance));
        verify(bean.besDao, times(1)).notifyAsyncOperationStatus(
                any(ServiceInstance.class), anyString(),
                eq(OperationStatus.RUNNING), anyListOf(LocalizedText.class));
    }

    @Test
    public void executeOperation_Suspend() throws Exception {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setRunWithTimer(true);
        doReturn(instance).when(bean.dao).getInstanceById(anyString());
        doReturn(instance).when(bean.em).getReference(
                eq(ServiceInstance.class), any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user, InstanceOperation.SUSPEND);

        // then
        verify(bean.dao, times(1)).suspendInstance(eq(instance));
    }

    @Test
    public void executeOperation_Unlock() throws Exception {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setLocked(true);
        doReturn(instance).when(bean.dao).getInstanceById(anyString());
        doReturn(instance).when(bean.em).getReference(
                eq(ServiceInstance.class), any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user, InstanceOperation.UNLOCK);

        // then
        verify(bean.dao, times(1)).unlockInstance(eq(instance));
    }

    @Test(expected = ServiceInstanceException.class)
    public void executeOperation_CompletePending_notReady()
            throws ServiceInstanceNotFoundException, ServiceInstanceException {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setControllerReady(false);
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        doReturn(instance).when(bean.em).getReference(
                eq(ServiceInstance.class), any(ServiceInstance.class));
        doReturn(instance).when(bean.dao).find(any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user,
                InstanceOperation.COMPLETE_PENDING);
    }

    @Test
    public void executeOperation_CompletePending_create()
            throws ServiceInstanceNotFoundException, ServiceInstanceException,
            BESNotificationException {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setControllerReady(true);
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        doReturn(instance).when(bean.dao).find(any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user,
                InstanceOperation.COMPLETE_PENDING);

        // then
        verify(bean.besDao, times(1)).notifyAsyncSubscription(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(true), any(APPlatformException.class));
        assertEquals(ProvisioningStatus.COMPLETED,
                instance.getProvisioningStatus());

    }

    @Test
    public void executeOperation_CompletePending_modify()
            throws ServiceInstanceNotFoundException, ServiceInstanceException,
            BESNotificationException {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setControllerReady(true);
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION);
        doReturn(instance).when(bean.dao).find(any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user,
                InstanceOperation.COMPLETE_PENDING);

        // then
        verify(bean.besDao, times(1)).notifyAsyncModifySubscription(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(true), any(APPlatformException.class));
        assertEquals(ProvisioningStatus.COMPLETED,
                instance.getProvisioningStatus());
    }

    @Test
    public void executeOperation_CompletePending_upgrade()
            throws ServiceInstanceNotFoundException, ServiceInstanceException,
            BESNotificationException {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setControllerReady(true);
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_UPGRADE);
        doReturn(instance).when(bean.dao).find(any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user,
                InstanceOperation.COMPLETE_PENDING);

        // then
        verify(bean.besDao, times(1)).notifyAsyncUpgradeSubscription(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(true), any(APPlatformException.class));
        assertEquals(ProvisioningStatus.COMPLETED,
                instance.getProvisioningStatus());
    }

    @Test
    public void executeOperation_CompletePending_Operation()
            throws ServiceInstanceNotFoundException, ServiceInstanceException,
            BESNotificationException {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setControllerReady(true);
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_OPERATION);
        doReturn(instance).when(bean.dao).find(any(ServiceInstance.class));
        doReturn(givenOperation()).when(bean.opDao).getOperationByInstanceId(
                anyString());

        // when
        bean.executeOperation(instance, user,
                InstanceOperation.COMPLETE_PENDING);

        // then
        verify(bean.besDao, times(1)).notifyAsyncOperationStatus(
                any(ServiceInstance.class), anyString(),
                eq(OperationStatus.COMPLETED), anyListOf(LocalizedText.class));
        assertEquals(ProvisioningStatus.COMPLETED,
                instance.getProvisioningStatus());
    }

    @Test
    public void executeOperation_AbortPending_create()
            throws ServiceInstanceNotFoundException, ServiceInstanceException,
            BESNotificationException {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        doReturn(instance).when(bean.dao).abortPendingInstance(
                any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user, InstanceOperation.ABORT_PENDING);

        // then
        verify(bean.besDao, times(1)).notifyAsyncSubscription(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(false), any(APPlatformException.class));
        assertEquals(ProvisioningStatus.COMPLETED,
                instance.getProvisioningStatus());
        assertTrue(instance.isControllerReady());
    }

    @Test
    public void executeOperation_AbortPending_modify_rollback()
            throws ServiceInstanceNotFoundException, ServiceInstanceException,
            BESNotificationException {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setRollbackParameters("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\r\n<properties>\r\n<entry key=\"KEY2\">VALUE2</entry>\r\n<entry key=\"ROLLBACK_SUBSCRIPTIONID\">subscriptionId</entry>\r\n<entry key=\"KEY1\">VALUE1</entry>\r\n</properties>\r\n");
        instance.setRollbackInstanceAttributes("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\r\n<properties>\r\n<entry key=\"KEY2\">VALUE2</entry>\r\n<entry key=\"ROLLBACK_SUBSCRIPTIONID\">subscriptionId</entry>\r\n<entry key=\"KEY1\">VALUE1</entry>\r\n</properties>\r\n");
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION);
        doReturn(instance).when(bean.dao).abortPendingInstance(
                any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user, InstanceOperation.ABORT_PENDING);

        // then
        verify(bean.besDao, times(1)).notifyAsyncModifySubscription(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(false), any(APPlatformException.class));
        assertTrue(instance.getInstanceParameters().size() > 0);
        assertEquals(ProvisioningStatus.COMPLETED,
                instance.getProvisioningStatus());
        assertTrue(instance.isControllerReady());
    }

    @Test
    public void executeOperation_AbortPending_modify_noRollback()
            throws ServiceInstanceNotFoundException, ServiceInstanceException,
            BESNotificationException {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setRollbackParameters(null);
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION);
        doReturn(instance).when(bean.dao).abortPendingInstance(
                any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user, InstanceOperation.ABORT_PENDING);

        // then
        verify(bean.besDao, times(1)).notifyAsyncModifySubscription(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(false), any(APPlatformException.class));
        assertTrue(instance.getInstanceParameters().isEmpty());
        assertEquals(ProvisioningStatus.COMPLETED,
                instance.getProvisioningStatus());
        assertTrue(instance.isControllerReady());
    }

    @Test
    public void executeOperation_AbortPending_upgrade_rollback()
            throws ServiceInstanceNotFoundException, ServiceInstanceException,
            BESNotificationException {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setRollbackParameters("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\r\n<properties>\r\n<entry key=\"KEY2\">VALUE2</entry>\r\n<entry key=\"ROLLBACK_SUBSCRIPTIONID\">subscriptionId</entry>\r\n<entry key=\"KEY1\">VALUE1</entry>\r\n</properties>\r\n");
        instance.setRollbackInstanceAttributes("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\r\n<properties>\r\n<entry key=\"KEY2\">VALUE2</entry>\r\n<entry key=\"ROLLBACK_SUBSCRIPTIONID\">subscriptionId</entry>\r\n<entry key=\"KEY1\">VALUE1</entry>\r\n</properties>\r\n");
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_UPGRADE);
        doReturn(instance).when(bean.dao).abortPendingInstance(
                any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user, InstanceOperation.ABORT_PENDING);

        // then
        verify(bean.besDao, times(1)).notifyAsyncUpgradeSubscription(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(false), any(APPlatformException.class));
        assertTrue(instance.getInstanceParameters().size() > 0);
        assertEquals(ProvisioningStatus.COMPLETED,
                instance.getProvisioningStatus());
        assertTrue(instance.isControllerReady());
    }

    @Test
    public void executeOperation_AbortPending_upgrade_noRollback()
            throws ServiceInstanceNotFoundException, ServiceInstanceException,
            BESNotificationException {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setRollbackParameters(null);
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_UPGRADE);
        doReturn(instance).when(bean.dao).abortPendingInstance(
                any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user, InstanceOperation.ABORT_PENDING);
        // then
        verify(bean.besDao, times(1)).notifyAsyncUpgradeSubscription(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(false), any(APPlatformException.class));
        assertTrue(instance.getInstanceParameters().isEmpty());
        assertEquals(ProvisioningStatus.COMPLETED,
                instance.getProvisioningStatus());
        assertTrue(instance.isControllerReady());
    }

    @Test
    public void executeOperation_AbortPending_Operation()
            throws ServiceInstanceNotFoundException, ServiceInstanceException,
            BESNotificationException {
        // given
        ServiceInstance instance = givenServiceInstance();
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_OPERATION);
        doReturn(givenOperation()).when(bean.opDao).getOperationByInstanceId(
                anyString());
        doReturn(instance).when(bean.dao).abortPendingInstance(
                any(ServiceInstance.class));

        // when
        bean.executeOperation(instance, user, InstanceOperation.ABORT_PENDING);

        // then
        verify(bean.besDao, times(1)).notifyAsyncOperationStatus(
                any(ServiceInstance.class), anyString(),
                eq(OperationStatus.ERROR), anyListOf(LocalizedText.class));
        assertTrue(instance.getInstanceParameters().isEmpty());
        assertEquals(ProvisioningStatus.COMPLETED,
                instance.getProvisioningStatus());
        assertTrue(instance.isControllerReady());
    }

    private ServiceInstance givenServiceInstance() {
        ServiceInstance si = new ServiceInstance();
        si.setInstanceId("InstanceID");
        return si;
    }

    private Operation givenOperation() {
        Operation op = new Operation();
        op.setTransactionId("transactionId");
        return op;
    }

}
