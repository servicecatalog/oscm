/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-9-18                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.techserviceoperationmgmt;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.techproductoperation.bean.OperationRecordServiceLocalBean;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.assembler.BasePOAssembler;
import org.oscm.internal.assembler.POOperationRecordAssembler;
import org.oscm.internal.types.enumtypes.OperationStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * @author maoq
 * 
 */
@Stateless
@Remote(OperationRecordService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class OperationRecordServiceBean implements OperationRecordService {

    @EJB(beanInterface = OperationRecordServiceLocalBean.class)
    OperationRecordServiceLocalBean operationRecordServiceLocalBean;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    protected LocalizerServiceLocal localizer;

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @Override
    public List<POOperationRecord> getOperationRecords(
            Boolean myOperationsOnly, String locale) {

        LocalizerFacade facade = new LocalizerFacade(localizer, locale);

        return POOperationRecordAssembler.toPOOperationRecords(
                operationRecordServiceLocalBean
                        .getOperationRecords(myOperationsOnly.booleanValue()),
                facade);
    }

    @Override
    public void updateOperationRecord(String transactionId,
            OperationStatus status, List<VOLocalizedText> progress)
            throws ValidationException, OperationNotPermittedException,
            ConcurrentModificationException, ObjectNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteOperationRecords(List<POOperationRecord> operationRecords)
            throws ObjectNotFoundException, ConcurrentModificationException {
        operationRecordServiceLocalBean
                .deleteOperationRecords(getOperationRecordKeysToDelete(operationRecords));
    }

    private List<Long> getOperationRecordKeysToDelete(
            List<POOperationRecord> records) throws ObjectNotFoundException,
            ConcurrentModificationException {
        ArgumentValidator.notNull("recordsToBeDeleted", records);
        List<Long> keys = new ArrayList<Long>();
        for (POOperationRecord record : records) {
            verifySubscription(record);
            verifyUser(record);
            keys.add(Long.valueOf(record.getKey()));
        }
        return keys;
    }

    private void verifySubscription(POOperationRecord record)
            throws ObjectNotFoundException, ConcurrentModificationException {
        Subscription subscription = dm.getReference(Subscription.class, record
                .getSubscription().getKey());
        BasePOAssembler.verifyVersionAndKey(subscription,
                record.getSubscription());
    }

    private void verifyUser(POOperationRecord record)
            throws ObjectNotFoundException, ConcurrentModificationException {
        PlatformUser user = dm.getReference(PlatformUser.class, record
                .getUser().getKey());
        BasePOAssembler.verifyVersionAndKey(user, record.getUser());
    }
}
