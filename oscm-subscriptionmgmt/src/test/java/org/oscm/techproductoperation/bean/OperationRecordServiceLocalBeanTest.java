/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 18, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.techproductoperation.bean;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.OperationRecord;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.UserRole;
import org.oscm.techproductoperation.dao.OperationRecordDao;
import org.oscm.internal.types.enumtypes.OperationStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationStateException;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * @author zhaoh.fnst
 * 
 */
public class OperationRecordServiceLocalBeanTest {

    private final OperationRecordServiceLocalBean bean = new OperationRecordServiceLocalBean();
    private final PlatformUser user = new PlatformUser();
    private final Organization org = new Organization();
    private final boolean onlyMyOperations = true;
    private final boolean notOnlyMyOperations = false;
    private final long userKey = 111L;
    private final long orgKey = 222L;

    @Before
    public void setup() throws Exception {
        bean.dm = mock(DataService.class);
        bean.operationRecordDao = mock(OperationRecordDao.class);
        when(bean.dm.getCurrentUser()).thenReturn(user);
    }

    @Test
    public void getOperationRecords_OnlyMyOperations_Admin() throws Exception {
        // given
        setUserRole(UserRoleType.ORGANIZATION_ADMIN);
        user.setKey(userKey);

        // when
        bean.getOperationRecords(onlyMyOperations);

        // then
        verify(bean.operationRecordDao, times(1)).getOperationsForUser(
                eq(userKey));
    }

    @Test
    public void getOperationRecords_OnlyMyOperations_SubManager()
            throws Exception {
        // given
        setUserRole(UserRoleType.SUBSCRIPTION_MANAGER);
        user.setKey(userKey);

        // when
        bean.getOperationRecords(onlyMyOperations);

        // then
        verify(bean.operationRecordDao, times(1)).getOperationsForUser(
                eq(userKey));
    }

    @Test
    public void getOperationRecords_NotOnlyMyOperations_Admin()
            throws Exception {
        // given
        user.setKey(userKey);

        // when
        bean.getOperationRecords(onlyMyOperations);

        // then
        verify(bean.operationRecordDao, times(1)).getOperationsForUser(
                eq(userKey));
    }

    @Test
    public void getOperationRecords_NotOnlyMyOperations_SubManager()
            throws Exception {
        // given
        setUserRole(UserRoleType.SUBSCRIPTION_MANAGER);
        user.setKey(userKey);

        // when
        bean.getOperationRecords(notOnlyMyOperations);

        // then
        verify(bean.operationRecordDao, times(1)).getOperationsForSubManager(
                eq(userKey));
    }

    @Test
    public void getOperationRecords_User() throws Exception {
        // given
        user.setKey(userKey);

        // when
        bean.getOperationRecords(onlyMyOperations);

        // then
        verify(bean.operationRecordDao, times(1)).getOperationsForUser(
                eq(userKey));
    }

    @Test(expected = OperationStateException.class)
    public void changeOperationStatus_OperationStateException()
            throws Exception {
        // given
        user.setKey(userKey);
        org.setKey(orgKey);
        user.setOrganization(org);
        OperationRecord record = givenOperationRecord(orgKey);
        record.setUser(user);
        record.setStatus(OperationStatus.COMPLETED);
        doReturn(record).when(bean.dm).getReferenceByBusinessKey(
                any(OperationRecord.class));

        // when
        bean.updateOperationStatus("transactionId", OperationStatus.ERROR,
                new ArrayList<VOLocalizedText>());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void changeOperationStatus_Organization_OperationNotPermittedException()
            throws Exception {
        // given
        user.setKey(userKey);
        org.setKey(orgKey);
        user.setOrganization(org);
        OperationRecord record = givenOperationRecord(7456L);
        record.setUser(user);
        record.setStatus(OperationStatus.RUNNING);
        doReturn(record).when(bean.dm).getReferenceByBusinessKey(
                any(OperationRecord.class));

        // when
        bean.updateOperationStatus("transactionId", OperationStatus.ERROR,
                new ArrayList<VOLocalizedText>());
    }

    private void setUserRole(UserRoleType type) {
        Set<RoleAssignment> grantedRoles = new HashSet<RoleAssignment>();
        RoleAssignment assignedRole = new RoleAssignment();
        UserRole role = new UserRole();
        role.setRoleName(type);
        assignedRole.setRole(role);
        grantedRoles.add(assignedRole);
        user.setAssignedRoles(grantedRoles);
    }

    private OperationRecord givenOperationRecord(long orgKey) {
        OperationRecord record = new OperationRecord();
        TechnicalProduct tp = new TechnicalProduct();
        Organization organization = new Organization();
        organization.setKey(orgKey);
        tp.setOrganization(organization);
        TechnicalProductOperation operation = new TechnicalProductOperation();
        operation.setTechnicalProduct(tp);
        record.setTechnicalProductOperation(operation);
        return record;
    }
}
