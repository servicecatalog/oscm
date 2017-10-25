/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 17, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.techproductoperation.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.OperationRecord;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.techproductoperation.dao.OperationRecordDao;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.enumtypes.OperationStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationStateException;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * @author zhaoh.fnst
 * 
 */
@Stateless
@LocalBean
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class OperationRecordServiceLocalBean {
    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @Inject
    public OperationRecordDao operationRecordDao;

    @Resource
    SessionContext sessionCtx;

    public List<OperationRecord> getOperationRecords(boolean onlyMyOperations) {
        ArgumentValidator.notNull("onlyMyOperations",
                Boolean.valueOf(onlyMyOperations));
        PlatformUser user = dm.getCurrentUser();
        if (onlyMyOperations) {
            return operationRecordDao.getOperationsForUser(user.getKey());
        }
        if (user.hasRole(UserRoleType.ORGANIZATION_ADMIN)) {
            return operationRecordDao.getOperationsForOrgAdmin(user
                    .getOrganization().getKey());
        } else if (user.hasRole(UserRoleType.SUBSCRIPTION_MANAGER)) {
            return operationRecordDao.getOperationsForSubManager(user.getKey());
        }

        return new ArrayList<OperationRecord>();
    }

    public void createOperationRecord(OperationRecord record)
            throws NonUniqueBusinessKeyException {
        ArgumentValidator.notNull("record", record);

        try {
            dm.persist(record);
            dm.flush();
        } catch (NonUniqueBusinessKeyException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    @RolesAllowed({ "TECHNOLOGY_MANAGER" })
    public void updateOperationStatus(String transactionId,
            OperationStatus status, List<VOLocalizedText> progress)
            throws OperationNotPermittedException, OperationStateException {
        ArgumentValidator.notNull("transactionId", transactionId);
        ArgumentValidator.notNull("operationStatus", status);

        OperationRecord record = new OperationRecord();
        record.setTransactionid(transactionId);
        OperationRecord currentRecord;
        try {
            currentRecord = (OperationRecord) dm
                    .getReferenceByBusinessKey(record);
        } catch (ObjectNotFoundException e) {
            return;
        }

        PlatformUser currentUser = dm.getCurrentUser();
        validateOrganizationKey(currentUser, currentRecord);
        validateOperationStatus(currentRecord.getStatus(), status);

        currentRecord.setStatus(status);
        dm.flush();

        if (progress == null || progress.isEmpty()) {
            localizer.removeLocalizedValues(currentRecord.getKey(),
                    LocalizedObjectTypes.OPERATION_STATUS_DESCRIPTION);
        } else {
            localizer
                    .storeLocalizedResources(currentRecord.getKey(),
                            LocalizedObjectTypes.OPERATION_STATUS_DESCRIPTION,
                            progress);
        }
    }

    public void deleteOperationRecords(List<Long> recordKeysToBeDeleted)
            throws ObjectNotFoundException {
        ArgumentValidator.notNull("recordKeysToBeDeleted",
                recordKeysToBeDeleted);
        try {
            for (Long key : recordKeysToBeDeleted) {
                OperationRecord recordToBeDeleted = dm.getReference(
                        OperationRecord.class, key.longValue());
                dm.remove(recordToBeDeleted);
            }
        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    public void removeOperationsForSubscription(long subscriptionKey) {
        operationRecordDao.removeOperationsForSubscription(subscriptionKey);
    }

    private void validateOperationStatus(OperationStatus currentStatus,
            OperationStatus newStatus) throws OperationStateException {
        if (currentStatus.equals(OperationStatus.COMPLETED)
                && !(newStatus.equals(OperationStatus.COMPLETED))) {
            throw new OperationStateException();
        }
    }

    private void validateOrganizationKey(PlatformUser currentUser,
            OperationRecord currentRecord)
            throws OperationNotPermittedException {
        if (currentRecord.getTechnicalProductOperation().getTechnicalProduct()
                .getOrganization().getKey() != (currentUser.getOrganization()
                .getKey())) {
            throw new OperationNotPermittedException();
        }
    }
}
