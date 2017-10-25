/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.billingadapter;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.BillingApplicationException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.DuplicateAdapterException;
import org.oscm.internal.types.exception.DuplicatePropertyKeyException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * Remote interface for the billing adapter service of the portal facade to be
 * used by the controller beans of the portal application.
 * 
 * @author stavreva
 * 
 */
@Remote
public interface BillingAdapterService {

    /**
     * Lists all billing adapters with basic info: key and billing identifier.
     * 
     * @return
     */
    Response getBaseBillingAdapters();

    Response getBillingAdapters();

    Response getDefaultBaseBillingAdapter();

    Response getBillingAdapter(String billingIdentifier);

    Response saveBillingAdapter(POBillingAdapter billingAdapter)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            SaaSApplicationException, DuplicateAdapterException,
            DuplicatePropertyKeyException, ConcurrentModificationException;

    Response setDefaultBillingAdapter(POBillingAdapter poBillingAdapter)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            DuplicateAdapterException, SaaSApplicationException;

    Response deleteAdapter(POBillingAdapter billingAdapter)
            throws DeletionConstraintException, ObjectNotFoundException;

    Response isActive(POBillingAdapter billingAdapter)
            throws SaaSApplicationException;

    Response testConnection(String billingIdentifier)
            throws BillingApplicationException;

    Response testConnection(POBillingAdapter billingAdapter)
            throws BillingApplicationException, SaaSApplicationException;

    Response getDefaultBillingAdapter();

}
