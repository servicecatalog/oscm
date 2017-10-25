/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingadapterservice.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingAdapter;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.DuplicateAdapterException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author stavreva
 * 
 */

@LocalBean
@Stateless
public class BillingAdapterDAO {

    @EJB(beanInterface = DataService.class)
    DataService ds;

    @RolesAllowed({ "PLATFORM_OPERATOR", "TECHNOLOGY_MANAGER" })
    public List<BillingAdapter> getAll() {
        Query query = ds.createNamedQuery("BillingAdapter.getAll");
        return ParameterizedTypes.list(query.getResultList(),
                BillingAdapter.class);
    }

    @RolesAllowed({ "PLATFORM_OPERATOR", "TECHNOLOGY_MANAGER" })
    public BillingAdapter get(String billingIdentifier) {
        BillingAdapter adapter = new BillingAdapter();
        adapter.setBillingIdentifier(billingIdentifier);
        adapter = get(adapter);
        return adapter;
    }

    @RolesAllowed({ "PLATFORM_OPERATOR", "TECHNOLOGY_MANAGER",
            "SERVICE_MANAGER" })
    public BillingAdapter get(BillingAdapter billingAdapter) {
        BillingAdapter ba = null;
        if (billingAdapter.getKey() != 0L) {
            ba = ds.find(BillingAdapter.class, billingAdapter.getKey());
        } else {
            ba = (BillingAdapter) ds.find(billingAdapter);
        }
        return ba;
    }

    @RolesAllowed({ "PLATFORM_OPERATOR", "TECHNOLOGY_MANAGER" })
    public BillingAdapter getDefault() {
        Query query = ds.createNamedQuery("BillingAdapter.getDefaultAdapter");
        return (BillingAdapter) query.getSingleResult();
    }

    @RolesAllowed({ "PLATFORM_OPERATOR", "TECHNOLOGY_MANAGER" })
    public boolean isActive(BillingAdapter billingAdapter) {
        Query query = ds.createNamedQuery("BillingAdapter.isActive");
        query.setParameter("billingIdentifier",
                billingAdapter.getBillingIdentifier());
        return !query.getResultList().isEmpty();

    }

    @RolesAllowed("PLATFORM_OPERATOR")
    public void save(BillingAdapter billingAdapter)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            DuplicateAdapterException {

        ArgumentValidator.notNull("Billing adapter", billingAdapter);

        try {

            if (billingAdapter.getKey() == 0L) {
                ds.persist(billingAdapter);
            } else {
                BillingAdapter ba = ds.getReference(BillingAdapter.class,
                        billingAdapter.getKey());
                ba.setBillingIdentifier(billingAdapter.getBillingIdentifier());
                ba.setName(billingAdapter.getName());
                ba.setDefaultAdapter(billingAdapter.isDefaultAdapter());
                ba.setConnectionProperties(billingAdapter
                        .getConnectionProperties());

                ds.flush();
            }
        } catch (EJBTransactionRolledbackException e) {

            if (isEntityExistsException(e)) {
                throw new DuplicateAdapterException(
                        "Duplicate adapter with unique id "
                                + billingAdapter.getBillingIdentifier());
            } else {
                throw e;
            }
        }

    }

    @RolesAllowed("PLATFORM_OPERATOR")
    public void setDefaultAdapter(BillingAdapter billingAdapter)
            throws EJBTransactionRolledbackException {

        ArgumentValidator.notNull("Billing adapter", billingAdapter);

        List<BillingAdapter> adapters = getAll();
        for (BillingAdapter ba : adapters) {

            if (ba.getBillingIdentifier().equals(
                    billingAdapter.getBillingIdentifier())) {
                ba.setDefaultAdapter(true);
            } else {
                ba.setDefaultAdapter(false);
            }
        }

        ds.flush();
    }

    @RolesAllowed("PLATFORM_OPERATOR")
    public void delete(BillingAdapter billingAdapter)
            throws DeletionConstraintException, ObjectNotFoundException {

        BillingAdapter ba = ds.getReference(BillingAdapter.class,
                billingAdapter.getKey());
        if (!ba.isDefaultAdapter() && !isActive(ba)) {
            ds.remove(ba);
        } else {

            DeletionConstraintException e = new DeletionConstraintException(
                    "It's not allowed to delete an active or the default billing adapter");
            if (ba.isDefaultAdapter()) {
                e.setMessageKey("error.billingAdapter.unableToDelete.default");
            } else {
                e.setMessageKey("error.billingAdapter.unableToDelete.active");
            }

            throw e;
        }
    }

    boolean isDefaultValid() {
        Query query = ds.createNamedQuery("BillingAdapter.getDefaultAdapter");
        List<BillingAdapter> defaultAdapters = ParameterizedTypes.list(
                query.getResultList(), BillingAdapter.class);
        if (defaultAdapters.isEmpty() || defaultAdapters.size() > 1) {
            return false;
        }
        return true;
    }

    // TODO move to data service
    boolean isEntityExistsException(final Throwable e) {
        if (e == null) {
            return false;
        }
        if (e instanceof PersistenceException) {
            return true;
        }
        if (e instanceof EJBException) {
            final EJBException ejbex = (EJBException) e;
            if (isEntityExistsException(ejbex.getCausedByException())) {
                return true;
            }
        }
        return isEntityExistsException(e.getCause());
    }

}
