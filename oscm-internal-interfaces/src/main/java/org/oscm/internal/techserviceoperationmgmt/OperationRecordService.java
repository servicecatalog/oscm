/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-9-18                                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.techserviceoperationmgmt;

import java.util.List;

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
public interface OperationRecordService {

    /**
     * get users operation records of specified user
     * 
     * @param myOperationsOnly
     *            if <true> get my operation records only
     *            <p>
     *            if <false> get operation records according to user role
     * @param locale
     *            The user's locale
     * @return POOperationRecord list
     * 
     * @throws OperationNotPermittedException
     * 
     */
    public List<POOperationRecord> getOperationRecords(
            Boolean myOperationsOnly, String locale);

    /**
     * update existing operation record
     * 
     * @param transactionId
     *            the transactionId of operation record
     * @param status
     *            the OperationStatus of operation record
     * @param progress
     *            the progress contains localized description of to operation
     *            status
     * 
     * @throws ValidationException
     * @throws OperationNotPermittedException
     * @throws ConcurrentModificationException
     * @throws ObjectNotFoundException
     */
    public void updateOperationRecord(String transactionId,
            OperationStatus status, List<VOLocalizedText> progress)
            throws ValidationException, OperationNotPermittedException,
            ConcurrentModificationException, ObjectNotFoundException;

    /**
     * delete operation records
     * 
     * @param poOperationRecords
     *            operation records list to be deleted
     * 
     * @throws ObjectNotFoundException
     * @throws ConcurrentModificationException
     */
    public void deleteOperationRecords(List<POOperationRecord> operationRecords)
            throws ObjectNotFoundException, ConcurrentModificationException;

}
