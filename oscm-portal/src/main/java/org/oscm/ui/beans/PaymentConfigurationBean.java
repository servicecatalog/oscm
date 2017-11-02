/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 06.10.2011                                                      
 *                                                                              
 *  Completion Time: 07.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOServicePaymentConfiguration;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.model.CustomerPaymentTypes;
import org.oscm.ui.model.PaymentTypes;
import org.oscm.ui.model.SelectedPaymentType;
import org.oscm.ui.model.ServicePaymentTypes;

/**
 * Handles the payment configuration a supplier can manage for his customers and
 * services.
 * 
 * @author weiser
 * 
 */
@ViewScoped
@ManagedBean(name = "paymentConfigurationBean")
public class PaymentConfigurationBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -4889463909553062668L;

    @EJB(beanInterface = ConfigurationService.class)
    private
    ConfigurationService cfgService;

    private Comparator<SelectedPaymentType> paymentTypeComparator = new Comparator<SelectedPaymentType>() {

        @Override
        public int compare(SelectedPaymentType o1, SelectedPaymentType o2) {
            return o1.getPaymentTypeId().compareTo(o2.getPaymentTypeId());
        }

    };

    private List<SelectedPaymentType> enabledPaymentTypes;
    private List<SelectedPaymentType> defaultPaymentTypes;
    private List<CustomerPaymentTypes> customerPaymentTypes;
    private List<CustomerPaymentTypes> customerPaymentTypesInServer;
    private List<SelectedPaymentType> defaultServicePaymentTypes;
    private List<ServicePaymentTypes> servicePaymentTypes;
    private List<ServicePaymentTypes> servicePaymentTypesInServer;

    /**
     * Initialized if necessary and returns the payment types that are enabled
     * by the platform operator for the supplier.
     * 
     * @return the enabled payment types
     */
    public List<SelectedPaymentType> getEnabledPaymentTypesForSupplier() {
        if (!getCfgService().isPaymentInfoAvailable()) {
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_WARN, BaseBean.WARNING_PAYMENT_TYPES_NOT_USED, null);
        }
        if (enabledPaymentTypes == null) {
            Set<VOPaymentType> availablePaymentTypes = getAccountingService()
                    .getAvailablePaymentTypesForOrganization();
            enabledPaymentTypes = new ArrayList<>();
            for (VOPaymentType pt : availablePaymentTypes) {
                enabledPaymentTypes.add(new SelectedPaymentType(pt));
            }
            Collections.sort(enabledPaymentTypes, paymentTypeComparator);
        }

        return enabledPaymentTypes;
    }

    /**
     * Calculate the number of grid columns needed depending on the number of
     * available payment types.
     * 
     * @return the number of needed grid columns
     */
    public int getNumOfPaymentColumns() {

        // multiplied by 2 because check box and label needed per entry
        int cols = getEnabledPaymentTypesForSupplier().size() * 2;

        return cols;
    }

    /**
     * Retrieve the default payment configuration - the returned list contains
     * the payment types an their selection status.
     * 
     * @return the default payment configuration
     */
    public List<SelectedPaymentType> getDefaultPaymentTypes() {

        if (defaultPaymentTypes == null) {
            Set<VOPaymentType> conf = getAccountingService()
                    .getDefaultPaymentConfiguration();
            defaultPaymentTypes = mapAvailableToEnabledPaymentTypes(conf);
        }

        return defaultPaymentTypes;
    }

    /**
     * Retrieve the customer payment configuration - the objects contained in
     * the returned list map the organization to the list of payment types with
     * their selection status.
     * 
     * @return the customer payment configuration
     */
    public List<CustomerPaymentTypes> getCustomerPaymentTypes() {

        if (customerPaymentTypes == null) {
            List<CustomerPaymentTypes> result = new ArrayList<CustomerPaymentTypes>();
            List<VOOrganizationPaymentConfiguration> conf = getAccountingService()
                    .getCustomerPaymentConfiguration();
            for (VOOrganizationPaymentConfiguration opc : conf) {
                CustomerPaymentTypes cpt = new CustomerPaymentTypes(
                        opc.getOrganization());
                cpt.setPaymentTypes(mapAvailableToEnabledPaymentTypes(opc
                        .getEnabledPaymentTypes()));
                result.add(cpt);
            }
            customerPaymentTypes = result;
            customerPaymentTypesInServer = duplicate(result);
        }

        return customerPaymentTypes;
    }

    @SuppressWarnings("unchecked")
    private <T extends PaymentTypes> List<T> duplicate(List<T> allPaymentTypes) {
        List<T> result = new ArrayList<T>();
        for (T paymentTypes : allPaymentTypes) {
            T copy = (T) paymentTypes.duplicate();
            result.add(copy);
        }
        return result;
    }

    /**
     * Retrieve the service payment configuration - the objects contained in the
     * returned list map the service to the list of payment types with their
     * selection status.
     * 
     * @return the service payment configuration
     */
    public List<ServicePaymentTypes> getServicePaymentTypes() {

        if (servicePaymentTypes == null) {
            List<ServicePaymentTypes> result = new ArrayList<ServicePaymentTypes>();
            List<VOServicePaymentConfiguration> conf = getAccountingService()
                    .getServicePaymentConfiguration(
                            PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
            for (VOServicePaymentConfiguration spc : conf) {
                ServicePaymentTypes spt = new ServicePaymentTypes(
                        spc.getService());
                spt.setPaymentTypes(mapAvailableToEnabledPaymentTypes(spc
                        .getEnabledPaymentTypes()));
                result.add(spt);
            }
            servicePaymentTypes = result;
            servicePaymentTypesInServer = duplicate(result);
        }

        return servicePaymentTypes;
    }

    /**
     * Save the default and customer specific payment configuration
     * 
     * @return the action outcome
     * @throws SaaSApplicationException
     *             on error when saving the configuration
     */
    public String modifyPaymentEnablement() throws SaaSApplicationException {

        Set<VOPaymentType> custDef = getEnabledPaymentTypeSet(defaultPaymentTypes);
        Set<VOPaymentType> svcDef = getEnabledPaymentTypeSet(defaultServicePaymentTypes);
        List<VOOrganizationPaymentConfiguration> cust = new ArrayList<VOOrganizationPaymentConfiguration>();
        if (customerPaymentTypes != null) {
            for (CustomerPaymentTypes type : modifiedCustomerPaymentTypes()) {
                VOOrganizationPaymentConfiguration conf = new VOOrganizationPaymentConfiguration();
                conf.setOrganization(type.getCustomer());
                conf.setEnabledPaymentTypes(getEnabledPaymentTypeSet(type
                        .getPaymentTypes()));
                cust.add(conf);
            }
        }
        List<VOServicePaymentConfiguration> svc = new ArrayList<VOServicePaymentConfiguration>();
        if (servicePaymentTypes != null) {
            for (ServicePaymentTypes type : modifiedServicePaymentTypes()) {
                VOServicePaymentConfiguration conf = new VOServicePaymentConfiguration();
                conf.setService(type.getService());
                conf.setEnabledPaymentTypes(getEnabledPaymentTypeSet(type
                        .getPaymentTypes()));
                svc.add(conf);
            }
        }
        boolean rc = getAccountingService().savePaymentConfiguration(custDef,
                cust, svcDef, svc);
        customerPaymentTypes = null;
        defaultPaymentTypes = null;
        servicePaymentTypes = null;
        defaultServicePaymentTypes = null;
        enabledPaymentTypes = null;

        addInfoOrProgressMessage(rc, INFO_PAYMENT_ENABLEMENT_SAVED, null);

        return OUTCOME_SUCCESS;
    }

    private List<CustomerPaymentTypes> modifiedCustomerPaymentTypes() {
        return modifiedPaymentTypes(customerPaymentTypes,
                customerPaymentTypesInServer);
    }

    private List<ServicePaymentTypes> modifiedServicePaymentTypes() {
        return modifiedPaymentTypes(servicePaymentTypes,
                servicePaymentTypesInServer);
    }

    private <T extends PaymentTypes> List<T> modifiedPaymentTypes(
            List<T> local, List<T> server) {
        List<T> result = new ArrayList<T>();
        for (int i = 0; i < local.size(); i++) {
            T modified = local.get(i);
            T original = server.get(i);
            if (!modified.isSelectionIdentical(original)) {
                result.add(modified);
            }
        }
        return result;
    }

    /**
     * Retrieve the default service payment configuration - the returned list
     * contains the payment types and their selection status.
     * 
     * @return the default service payment configuration
     */
    public List<SelectedPaymentType> getDefaultServicePaymentTypes() {

        if (defaultServicePaymentTypes == null) {
            Set<VOPaymentType> conf = getAccountingService()
                    .getDefaultServicePaymentConfiguration();
            defaultServicePaymentTypes = mapAvailableToEnabledPaymentTypes(conf);
        }

        return defaultServicePaymentTypes;
    }

    /**
     * Puts the selected payment types in a set and returns it.
     * 
     * @param paymentTypes
     *            the list of payment types with their selection status
     * @return the set of selected payment types
     */
    private static Set<VOPaymentType> getEnabledPaymentTypeSet(
            List<SelectedPaymentType> paymentTypes) {

        Set<VOPaymentType> result = new HashSet<VOPaymentType>();
        if (paymentTypes != null) {
            for (SelectedPaymentType type : paymentTypes) {
                if (type.isSelected()) {
                    result.add(type.getPaymentType());
                }
            }
        }

        return result;
    }

    /**
     * Maps the available to the enabled payment types - a
     * {@link SelectedPaymentType} will be created for each available payment
     * type and it will be set to selected when the payment type is enabled
     * (contained in the passed set).
     * 
     * @param conf
     *            the enabled payment types
     * @return the list of payment types with their selection status
     */
    private List<SelectedPaymentType> mapAvailableToEnabledPaymentTypes(
            Set<VOPaymentType> conf) {
        List<SelectedPaymentType> types = getEnabledPaymentTypesForSupplier();
        List<SelectedPaymentType> result = new ArrayList<SelectedPaymentType>();
        for (SelectedPaymentType type : types) {
            SelectedPaymentType defaultType = new SelectedPaymentType(
                    type.getPaymentType());
            defaultType.setSelected(conf.contains(type.getPaymentType()));
            result.add(defaultType);
        }
        return result;
    }

    /**
     * @return the cfgService
     */
    public ConfigurationService getCfgService() {
        return cfgService;
    }

    /**
     * @param cfgService the cfgService to set
     */
    public void setCfgService(ConfigurationService cfgService) {
        this.cfgService = cfgService;
    }

}
