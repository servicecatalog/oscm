/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 21.10.2010                                                      
 *                                                                              
 *  Completion Time: 25.10.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.model.UdaDefinitionRow;
import org.oscm.ui.model.UdaRow;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * The bean for accessing UDA functionality
 * 
 * @author weiser
 * 
 */
@ViewScoped
@ManagedBean(name = "udaBean")
public class UdaBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 5737133057966931570L;

    private List<VOUdaDefinition> udaDefinitions = null;
    private boolean dirty = false;

    private List<UdaDefinitionRow> customerUdaDefinitions = null;
    private List<UdaDefinitionRow> subscriptionUdaDefinitions = null;

    /**
     * @return The list of {@link UdaDefinitionRow}s defined for customers.
     */
    public List<UdaDefinitionRow> getCustomerUdaDefinitions() {
        if (customerUdaDefinitions == null) {
            customerUdaDefinitions = getRowsForType(CUSTOMER,
                    getUdaDefinitions());
        }
        return customerUdaDefinitions;
    }

    /**
     * @return The list of {@link UdaDefinitionRow}s defined for subscriptions.
     */
    public List<UdaDefinitionRow> getSubscriptionUdaDefinitions() {
        if (subscriptionUdaDefinitions == null) {
            subscriptionUdaDefinitions = getRowsForType(CUSTOMER_SUBSCRIPTION,
                    getUdaDefinitions());
        }
        return subscriptionUdaDefinitions;
    }

    /**
     * @return The list of all {@link VOUdaDefinition}s from server.
     */
    private List<VOUdaDefinition> getUdaDefinitions() {
        if (udaDefinitions == null) {
            udaDefinitions = getAccountingService().getUdaDefinitions();
        }
        return udaDefinitions;
    }

    /**
     * Returns all {@link UdaDefinitionRow}s for the input list that have the
     * provided type.
     * 
     * @param type
     *            the target type
     * @param all
     *            the list to search in
     * @return the list of {@link UdaDefinitionRow}s with the specified target
     *         type
     */
    private static final List<UdaDefinitionRow> getRowsForType(String type,
            List<VOUdaDefinition> all) {
        List<UdaDefinitionRow> result = new ArrayList<UdaDefinitionRow>();
        for (VOUdaDefinition def : all) {
            if (type.equals(def.getTargetType())) {
                result.add(new UdaDefinitionRow(def));
            }
        }
        return result;
    }

    /**
     * Creates an new {@link VOUdaDefinition} of target type CUSTOMER and adds
     * it to the list of definitions for customers.
     * 
     * @return the result string
     */
    public String addCustomerUdaDefinition() {
        customerUdaDefinitions = add(CUSTOMER, customerUdaDefinitions);
        dirty = true;
        return OUTCOME_SUCCESS;
    }

    /**
     * Creates an new {@link VOUdaDefinition} of target type
     * CUSTOMER_SUBSCRIPTION and adds it to the list of definitions for
     * subscriptions.
     * 
     * @return the result string
     */
    public String addSubscriptionUdaDefinition() {
        subscriptionUdaDefinitions = add(CUSTOMER_SUBSCRIPTION,
                subscriptionUdaDefinitions);
        dirty = true;
        return OUTCOME_SUCCESS;
    }

    /**
     * Adds a new {@link VOUdaDefinition} of the provided type to the provided
     * list.
     * 
     * @param type
     *            the definition target type
     * @param list
     *            the list to add the created {@link VOUdaDefinition} to
     * @return the modified list
     */
    private static final List<UdaDefinitionRow> add(String type,
            List<UdaDefinitionRow> list) {
        VOUdaDefinition def = new VOUdaDefinition();
        def.setConfigurationType(UdaConfigurationType.SUPPLIER);
        def.setTargetType(type);
        UdaDefinitionRow udaDefinitionRow = new UdaDefinitionRow(def);
        udaDefinitionRow.setNewDefinition(true);
        list.add(udaDefinitionRow);
        return list;
    }

    /**
     * Save the not selected uda definitions and delete the selected ones.
     * 
     * @return the logical outcome
     * @throws SaaSApplicationException
     */
    public String saveDefinitions() throws SaaSApplicationException {
        List<VOUdaDefinition> toSave = new ArrayList<VOUdaDefinition>();
        List<VOUdaDefinition> toDelete = new ArrayList<VOUdaDefinition>();
        for (UdaDefinitionRow row : customerUdaDefinitions) {
            if (row.isSelected() && row.getDefinition().getKey() > 0) {
                toDelete.add(row.getDefinition());
            } else if (!row.isSelected()) {
                toSave.add(row.getDefinition());
            }
        }
        for (UdaDefinitionRow row : subscriptionUdaDefinitions) {
            if (row.isSelected() && row.getDefinition().getKey() > 0) {
                toDelete.add(row.getDefinition());
            } else if (!row.isSelected()) {
                toSave.add(row.getDefinition());
            }
        }
        try {
            getAccountingService().saveUdaDefinitions(toSave, toDelete);
        } finally {
            dirty = false;
        }
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_UDADEFINITIONS_SAVED);
        resetDefinitionLists();
        return OUTCOME_SUCCESS;
    }

    /**
     * Reset all definition lists to <code>null</code>.
     */
    private void resetDefinitionLists() {
        udaDefinitions = null;
        customerUdaDefinitions = null;
        subscriptionUdaDefinitions = null;
    }

    /**
     * Returns the list of uda definitions for the calling organization with the
     * provided target type.
     * 
     * @param type
     *            the target type
     * @return the list of {@link VOUdaDefinition}
     */
    List<VOUdaDefinition> getForType(String type) {
        List<VOUdaDefinition> result = new ArrayList<VOUdaDefinition>();
        for (VOUdaDefinition def : getUdaDefinitions()) {
            if (type.equals(def.getTargetType())) {
                result.add(def);
            }
        }
        return result;
    }

    /**
     * Returns {@link UdaRow}s for each defined customer uda - the
     * {@link VOUdaDefinition} either mapped to an existing or to a new
     * {@link VOUda}
     * 
     * @param key
     *            the customer key
     * @return the resulting list of {@link UdaRow}s
     * @throws ValidationException
     * @throws OrganizationAuthoritiesException
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     */
    List<UdaRow> getCustomerUdas(long key) throws ValidationException,
            OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException {
        List<VOUda> udas = getAccountingService().getUdas(UdaBean.CUSTOMER,
                key, false);
        return UdaRow.getUdaRows(getForType(UdaBean.CUSTOMER), udas);
    }

    /**
     * Returns {@link UdaRow}s for each defined subscription uda - the
     * {@link VOUdaDefinition} either mapped to an existing or to a new
     * {@link VOUda}
     * 
     * @param key
     *            the subscription key
     * @return the resulting list of {@link UdaRow}s
     * @throws ValidationException
     * @throws OrganizationAuthoritiesException
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     */
    List<UdaRow> getSubscriptionUdas(long key) throws ValidationException,
            OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException {
        List<VOUda> udas = getAccountingService().getUdas(
                UdaBean.CUSTOMER_SUBSCRIPTION, key, false);
        return UdaRow.getUdaRows(getForType(UdaBean.CUSTOMER_SUBSCRIPTION),
                udas);
    }

    public boolean isDirty() {
        return dirty;
    }

}
