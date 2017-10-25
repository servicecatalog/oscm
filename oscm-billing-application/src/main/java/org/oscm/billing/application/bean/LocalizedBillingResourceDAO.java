/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.application.bean;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.LocalizedBillingResource;
import org.oscm.internal.types.exception.BillingApplicationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * The DAO implementation for CRUD operations with localized billing resources.
 * 
 * @author stavreva
 * 
 */

@Stateless
@LocalBean
public class LocalizedBillingResourceDAO {

    @EJB(beanInterface = org.oscm.dataservice.local.DataService.class)
    DataService dm;

    /**
     * Loads the localized billing resource using its database or business key.
     * 
     * @param localizedBillingResource
     *            localized billing resource with key or business key set
     * @return localized billing resource
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public LocalizedBillingResource get(
            org.oscm.domobjects.LocalizedBillingResource localizedBillingResource) {
        if (localizedBillingResource == null) {
            return null;
        }
        if (localizedBillingResource.getKey() != 0L) {
            return dm.find(LocalizedBillingResource.class,
                    localizedBillingResource.getKey());
        }
        return (LocalizedBillingResource) dm.find(localizedBillingResource);
    }

    /**
     * Loads the localized billing resources using its database or business key.
     * 
     * @param localizedBillingResources
     *            list of localized billing resource with their key or business
     *            key set
     * @return list of localized billing resources
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<LocalizedBillingResource> get(
            List<LocalizedBillingResource> localizedBillingResources) {
        List<LocalizedBillingResource> resourceList = new ArrayList<LocalizedBillingResource>();
        if (localizedBillingResources != null) {
            for (LocalizedBillingResource resource : localizedBillingResources) {
                resource = get(resource);
                resourceList.add(resource);
            }
        }
        return resourceList;
    }

    /**
     * Update all given localized billing resources.
     * 
     * @param list
     *            of localizedBillingResources to be updated.
     * @param list
     *            of persistent localized billing resources
     * @throws BillingApplicationException
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<LocalizedBillingResource> update(
            List<LocalizedBillingResource> localizedBillingResources)
            throws BillingApplicationException {

        List<LocalizedBillingResource> dbResources = new ArrayList<LocalizedBillingResource>();
        for (LocalizedBillingResource localizedBillingResource : localizedBillingResources) {
            dbResources.add(update(localizedBillingResource));
        }

        return dbResources;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public LocalizedBillingResource update(
            LocalizedBillingResource localizedBillingResource) {
        LocalizedBillingResource resourceInDB = get(localizedBillingResource);
        if (resourceInDB != null) {
            resourceInDB.setDataType(localizedBillingResource.getDataType());
            resourceInDB.setValue(localizedBillingResource.getValue());
            return resourceInDB;
        }
        try {
            resourceInDB = localizedBillingResource;
            dm.persist(resourceInDB);
        } catch (NonUniqueBusinessKeyException e) {
            // concurrent access - localized billing resource meanwhile
            // exists in the database...
            resourceInDB = get(localizedBillingResource);
            if (resourceInDB != null) {
                resourceInDB
                        .setDataType(localizedBillingResource.getDataType());
                resourceInDB.setValue(localizedBillingResource.getValue());
            }
        }
        return resourceInDB;
    }

}