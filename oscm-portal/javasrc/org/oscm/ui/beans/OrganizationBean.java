/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.beans;

import java.io.IOException;
import java.io.Serializable;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.Part;

import org.oscm.converter.PropertiesLoader;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.vo.*;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.ImageUploader;
import org.oscm.ui.model.Organization;
import org.oscm.ui.model.TechnicalService;
import org.oscm.ui.model.UdaRow;
import org.oscm.ui.model.User;

/**
 * Backing bean for organization related actions
 * 
 */
@ViewScoped
@ManagedBean(name = "organizationBean")
public class OrganizationBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 4882370488318836006L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(OrganizationBean.class);

    private static final String CONCURRENT_MODIFICATION_ERROR = "concurrentModification";
    private static final String APPLICATION_BEAN = "appBean";

    private VOOrganization customerToAdd = null;
    private User customerUserToAdd = null;
    private List<Organization> suppliersForTechnicalService;
    private List<Organization> customers;
    private Part organizationProperties;
    VOOrganization organization;
    private String supplierIdToAdd;
    User currentUser;
    private Organization selectedCustomer;
    private byte[] billingData;
    private ImageUploader imageUploader = new ImageUploader(
            ImageType.ORGANIZATION_IMAGE);

    private List<UdaRow> customerUdaRows;

    @ManagedProperty(value = "#{menuBean}")
    private MenuBean menuBean;

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    @ManagedProperty(value = "#{udaBean}")
    private UdaBean udaBean;

    @ManagedProperty(value = "#{techServiceBean}")
    private TechServiceBean techServiceBean;

    private String marketplaceId;
    private boolean ldapManaged;
    private boolean internalAuthMode;
    private ApplicationBean appBean;

    // has the user confirmed the action?
    private boolean confirmed;

    private boolean showConfirm;

    private ArrayList<String> instanceIdsForSuppliers;
    
    /**
     * Sort NameWithOrganizationId in locale-sensitive alphabetical order.
     */
    private class NameWithOrgIdComparator implements Comparator<Organization> {
        Collator collator = Collator.getInstance();

        @Override
        public int compare(Organization o1, Organization o2) {
            return collator.compare(o1.getNameWithOrganizationId(), o2.getNameWithOrganizationId());
        }
    }
    
    /**
     * Sort OrganizationId in locale-sensitive alphabetical order.
     */
    private class OrgIdComparator implements Comparator<Organization> {
        Collator collator = Collator.getInstance();

        @Override
        public int compare(Organization o1, Organization o2) {
            return collator.compare(o1.getOrganizationId(), o2.getOrganizationId());
        }
    }

    public OrganizationBean() {
        super();
    }

    @Override
    public String getMarketplaceId() {
        return marketplaceId;
    }

    @Override
    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    public void setMenuBean(MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public void setUdaBean(UdaBean udaBean) {
        this.udaBean = udaBean;
    }

    public UdaBean getUdaBean() {
        return udaBean;
    }

    public TechServiceBean getTechServiceBean() {
        return techServiceBean;
    }

    public void setTechServiceBean(TechServiceBean techServiceBean) {
        this.techServiceBean = techServiceBean;
    }

    /**
     * Get a list of all customer of the current supplier.
     * 
     * @return a list with all customer of the current supplier.
     */
    public List<Organization> getCustomers() {
        if (customers == null) {
            Vo2ModelMapper<VOOrganization, Organization> mapper = new Vo2ModelMapper<VOOrganization, Organization>() {
                @Override
                public Organization createModel(final VOOrganization vo) {
                    Organization model = new Organization(vo);
                    model.setSelected(false);
                    return model;
                }
            };
            try {
                customers = mapper.map(getAccountingService()
                        .getMyCustomersOptimization());
                Collections.sort(customers, new NameWithOrgIdComparator());
            } catch (OrganizationAuthoritiesException e) {
                ExceptionHandler.execute(e);
            }
        }
        return customers;
    }

    /**
     * Get the id of the selected customer
     * 
     * @return the id of the selected customer
     */
    public String getSelectedCustomerId() {
        Organization customer = getSelectedCustomer();
        if (customer == null) {
            return null;
        }
        return customer.getOrganizationId();
    }

    /**
     * Select the customer with the given organizationId.
     * 
     * @param organizationId
     *            the id of the organization to select
     */
    public void setSelectedCustomerId(String organizationId) {
        selectedCustomer = null;
        customerUdaRows = null;
        sessionBean.setSelectedCustomerId(null);
        for (Organization customer : getCustomers()) {
            VOOrganization voCustomer = customer.getVOOrganization();
            if (customer.getOrganizationId().equals(organizationId)) {
                try {
                    VOOrganization vo = getAccountingService().getMyCustomer(
                            customer.getVOOrganization(),
                            getUserFromSession().getLocale());
                    voCustomer = vo;
                } catch (ObjectNotFoundException e) {
                    ExceptionHandler.execute(e);
                }
                customer.setVOOrganization(voCustomer);
                selectedCustomer = customer;
                sessionBean.setSelectedCustomerId(organizationId);
                break;
            }
        }
    }

    /**
     * Update VO for saved selected customer.
     * 
     * @param organizationId
     *            Organization ID of needed customer.
     * @param voOrganization
     *            VO for set for needed organization.
     */
    public void updateSelectedCustomer(String organizationId,
            VOOrganization voOrganization) {
        for (int i = 0; i < customers.size(); i++) {
            Organization customer = customers.get(i);
            if (customer.getOrganizationId().equals(organizationId)) {
                customer.setVOOrganization(voOrganization);
                break;
            }
        }
    }

    /**
     * Get the selected customer object
     * 
     * @return the selected customer object
     */
    public Organization getSelectedCustomer() {
        String customerId = sessionBean.getSelectedCustomerId();
        if (selectedCustomer == null && customerId != null) {
            setSelectedCustomerId(customerId);
        }
        return selectedCustomer;
    }

    /**
     * Get the current organization
     * 
     * @return the current organization
     */
    public VOOrganization getOrganization() {
        if (organization == null) {
            organization = getAccountingService().getOrganizationData();
        }
        return organization;
    }

    /**
     * Delete the current organization
     * 
     * @return the logical outcome.
     * @throws DeletionConstraintException
     *             Thrown if the organization has active subscriptions
     */
    public String delete() throws DeletionConstraintException {
        if (logger.isDebugLoggingEnabled()) {

        }
        try {
            getAccountingService().deregisterOrganization();
        } catch (TechnicalServiceNotAliveException
                | TechnicalServiceOperationException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_ORGANIZATION_DELETION_FAILED);
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_DELETE_USER_FROM_EXPIRED_SUBSCRIPTION);
            return "";
        }
        if (logger.isDebugLoggingEnabled()) {

        }
        return OUTCOME_LOGIN;
    }

    /**
     * Save the current organization and user profile
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     * @throws ImageException
     *             Thrown in case the access to the uploaded file failed.
     */
    public String save() throws SaaSApplicationException {

        VOUserDetails user = null;
        if (currentUser != null) {
            user = currentUser.getVOUserDetails();
        }
        getAccountingService().updateAccountInformation(organization, user,
                getMarketplaceId(), getImageUploader().getVOImageResource());
        currentUser = null;
        if (user != null) {
            // update the value object in the session
            setUserInSession(getCurrentUser().getVOUserDetails());
            addMessage(null, FacesMessage.SEVERITY_INFO,
                    INFO_USER_PROFILE_SAVED, getUserFromSession().getUserId());
        } else if (organization != null) {
            addMessage(null, FacesMessage.SEVERITY_INFO,
                    INFO_ORGANIZATION_SAVED, getUserFromSession()
                            .getOrganizationId());
        }
        organization = null; // load the organization again

        return OUTCOME_SUCCESS;
    }

    public List<Organization> getSuppliersForTechnicalService()
            throws ObjectNotFoundException, OperationNotPermittedException {
        if (logger.isDebugLoggingEnabled()) {

        }

        VOTechnicalService voTechnicalService = getSelectedTechnicalService();
        if (suppliersForTechnicalService == null && voTechnicalService != null) {

            List<VOOrganization> suppliers = getAccountingService()
                    .getSuppliersForTechnicalService(voTechnicalService);

            suppliersForTechnicalService = new ArrayList<Organization>();
            instanceIdsForSuppliers = null;
            for (VOOrganization org : suppliers) {
                suppliersForTechnicalService.add(new Organization(org));
            }
        }
        if (logger.isDebugLoggingEnabled()) {

        }
        if (suppliersForTechnicalService != null
                && suppliersForTechnicalService.size() > 1) {
            Collections.sort(suppliersForTechnicalService,
                    new OrgIdComparator());
        }
        return suppliersForTechnicalService;
    }

    /**
     * Check if any supplier is selected.
     */
    boolean isAnySupplierSelectedForDeletion() throws ObjectNotFoundException,
            OperationNotPermittedException {
        for (Organization org : getSuppliersForTechnicalService()) {
            if (org.isSelected())
                return true;
        }
        return false;
    }

    public boolean isDeleteSupplierEnabled() {
        try {
            return isAnySupplierSelectedForDeletion();
        } catch (ObjectNotFoundException e) {
            concurrentModification();
        } catch (OperationNotPermittedException e) {
            concurrentModification();
        }
        return false;
    }

    /**
     * execute navigation rule: go to destination specified for concurrent
     * modification situation
     */
    void concurrentModification() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getApplication().getNavigationHandler()
                .handleNavigation(ctx, "", CONCURRENT_MODIFICATION_ERROR);
        ctx.responseComplete();
    }

    public boolean getExistSuppliersForTechnicalService()
            throws ObjectNotFoundException, OperationNotPermittedException {
        // Since the list will be cached and used in case there are suppliers
        // present, it's okay to retrieve the list here.
        return !getSuppliersForTechnicalService().isEmpty();
    }

    public String addSuppliersForTechnicalService()
            throws AddMarketingPermissionException, ObjectNotFoundException,
            OperationNotPermittedException {

        if (!isTokenValid()) {
            return OUTCOME_SUCCESS;
        }

        VOTechnicalService voTechnicalService = getSelectedTechnicalService();
        if (voTechnicalService != null && !isBlank(supplierIdToAdd)) {
            List<String> ids = new ArrayList<String>();
            ids.add(supplierIdToAdd);
            try {
                getAccountingService().addSuppliersForTechnicalService(
                        voTechnicalService, ids);
            } finally {
                suppliersForTechnicalService = null;
                instanceIdsForSuppliers = null;
            }
            addMessage(null, FacesMessage.SEVERITY_INFO, INFO_SUPPLIER_ADDED,
                    supplierIdToAdd);
            resetToken();
            supplierIdToAdd = null;
        }

        return OUTCOME_SUCCESS;
    }

    /**
     * Called by the value changed listener of the technical service
     * selectionOneMenu.
     */
    public void technicalServiceChanged(ValueChangeEvent event) {
        Long newServiceKey = (Long) event.getNewValue();
        techServiceBean
                .setSelectedTechnicalServiceKeyWithExceptionAndRefresh(newServiceKey
                        .longValue());
        supplierIdToAdd = null;
        // Force a refresh of the displayed suppliers
        suppliersForTechnicalService = null;
    }

    /**
     * Small convince method to retrieve the selected technical service from the
     * corresponding bean and remove the UI wrapping.
     */
    private VOTechnicalService getSelectedTechnicalService() {
        TechnicalService ts = techServiceBean.getSelectedTechnicalService();
        if (ts != null && ts.getVo() != null) {
            return ts.getVo();
        }
        return null;
    }

    public String removeSuppliersFromTechnicalService()
            throws ObjectNotFoundException, OrganizationAuthoritiesException,
            OperationNotPermittedException,
            MarketingPermissionNotFoundException {

        if (!isTokenValid()) {
            return OUTCOME_SUCCESS;
        }

        VOTechnicalService voTechnicalService = getSelectedTechnicalService();
        String[] supplierIds = getSelectedOrganizationIds();

        showConfirm = false;
        if (supplierIds.length > 0 && voTechnicalService != null) {
            List<String> ids = Arrays.asList(supplierIds);
            instanceIdsForSuppliers = new ArrayList<String>();
            if (isConfirmed()) {
                getAccountingService().removeSuppliersFromTechnicalService(
                        voTechnicalService, ids);

                suppliersForTechnicalService = null;
                instanceIdsForSuppliers = null;

                addMessage(null, FacesMessage.SEVERITY_INFO,
                        INFO_SUPPLIER_REMOVED, supplierIdToAdd);

                resetToken();
            } else {
                List<String> instanceIds = getProvisioningService()
                        .getInstanceIdsForSellers(ids);
                if (instanceIds != null) {
                    instanceIdsForSuppliers.addAll(instanceIds);
                }
                showConfirm = true;
            }
        }

        return OUTCOME_SUCCESS;
    }

    protected String[] getSelectedOrganizationIds() {
        if (suppliersForTechnicalService == null
                || suppliersForTechnicalService.isEmpty()) {
            return new String[] {};
        }
        ArrayList<String> list = new ArrayList<String>();
        for (Organization org : suppliersForTechnicalService) {
            if (org.isSelected()) {
                list.add(org.getOrganizationId());
            }
        }
        String[] result = new String[list.size()];
        result = list.toArray(result);
        return result;
    }

    public void setSupplierIdToAdd(String supplierIdToAdd) {
        this.supplierIdToAdd = supplierIdToAdd;
    }

    public String getSupplierIdToAdd() {
        return supplierIdToAdd;
    }

    public boolean isDeleteEnabled() {
        return getSelectedOrganizationIds().length == 0;
    }

    public VOOrganization getCustomerToAdd() {
        if (customerToAdd == null) {
            customerToAdd = new VOOrganization();
            User user = getUserFromSession();
            customerToAdd.setLocale(user.getLocale());
            customerToAdd.setTenantKey(user.getTenantKey());
        }
        return customerToAdd;
    }

    void setCustomerToAdd(VOOrganization customer) {
        customerToAdd = customer;
    }

    public User getCustomerUserToAdd() throws MarketplaceRemovedException {
        if (customerUserToAdd == null) {
            customerUserToAdd = new User(new VOUserDetails());
            customerUserToAdd.getVOUserDetails().setTenantId(
                    sessionBean.getTenantID());
        }
        return customerUserToAdd;
    }

    public String addCustomer() throws NonUniqueBusinessKeyException,
            OrganizationAuthoritiesException, ValidationException,
            MailOperationException, ObjectNotFoundException,
            OperationPendingException {

        try {
            Properties properties = null;
            if (ldapManaged && organizationProperties != null) {
                properties = PropertiesLoader
                        .loadProperties(organizationProperties.getInputStream());
            }

            VOUserDetails user = customerUserToAdd.getVOUserDetails();
            user.setLocale(customerToAdd.getLocale());
            VOOrganization org = getAccountingService().registerKnownCustomer(
                    customerToAdd, user, LdapProperties.get(properties),
                    marketplaceId);

            // data must be empty for the next customer to create
            customerToAdd = null;
            customerUserToAdd = null;

            String orgId = "";
            if (org != null) {
                orgId = org.getOrganizationId();
                sessionBean.setSelectedCustomerId(orgId);
            }
            addInfoOrProgressMessage(org != null, INFO_ORGANIZATION_CREATED,
                    orgId);
        } catch (IOException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_ADD_CUSTOMER);
            addMessage(null, FacesMessage.SEVERITY_ERROR, ERROR_UPLOAD);
            return OUTCOME_ERROR;
        }

        return OUTCOME_SUCCESS;
    }

    public Part getOrganizationProperties() {
        return organizationProperties;
    }

    public void setOrganizationProperties(Part organizationProperties) {
        this.organizationProperties = organizationProperties;
    }

    /**
     * Writes the billing data to the response if existing.
     * 
     * @return the logical outcome
     * @throws IOException
     */
    public String showCustomerBillingData() throws IOException {
        if (billingData == null) {

            return OUTCOME_ERROR;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        String filename = sdf.format(Calendar.getInstance().getTime())
                + "_BillingData.xml";
        writeContentToResponse(billingData, filename, "text/xml");
        billingData = null;

        return OUTCOME_SUCCESS;
    }

    /**
     * Checks if billing data has been read.
     * 
     * @return <code>true</code> if billing data is available otherwise
     *         <code>false</code>.
     */
    public boolean isBillingDataAvailable() {
        return billingData != null;
    }

    /** xhtml component id for setting thru period error on update */
    private static final String COMPONENT_THRU_ID = "editForm:discountThru";

    public void resetCurrentUser() {
        currentUser = null;
    }

    public String getInitialize() {
        if (currentUser == null) {
            currentUser = getCurrentUser();
            getApplicationBean().checkLocaleValidation(currentUser.getLocale());
        }
        return "";
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            currentUser = new User(getIdService().getCurrentUserDetails());
            initializeGroups();
        }
        return currentUser;
    }

    void initializeGroups() {
        StringBuilder groupsToDisplay = new StringBuilder();
        List<POUserGroup> groups = getUserGroupService()
                .getUserGroupsForUserWithoutDefault(currentUser.getKey());
        for (POUserGroup group : groups) {
            groupsToDisplay.append(group.getGroupName());
            groupsToDisplay.append(", ");
        }
        if (groupsToDisplay.length() > 0) {
            groupsToDisplay.replace(groupsToDisplay.length() - 2,
                    groupsToDisplay.length(), "");
        }
        currentUser.setGroupsToDisplay(groupsToDisplay.toString());
        currentUser.setUserGroup(groups);
    }

    public User refreshCurrentUser() {
        currentUser = new User(getIdService().getCurrentUserDetails());
        return currentUser;
    }

    /**
     * Save customer after update.
     * 
     * @return Result of update.
     */
    public String updateCustomer() throws SaaSApplicationException {

        VOOrganization voOrganization = selectedCustomer.getVOOrganization();
        // clear old values as result these fields are disabled and VO is
        // not updated
        VODiscount voDiscount = voOrganization.getDiscount();
        if (voDiscount != null && voDiscount.getValue() == null) {
            voDiscount = null;
            voOrganization.setDiscount(voDiscount);
        }

        if (voDiscount != null) {
            Long begin = voOrganization.getDiscount().getStartTime();
            Long end = voOrganization.getDiscount().getEndTime();
            long currentTimeMonthYear = getTimeInMillisForFirstDay(System
                    .currentTimeMillis());
            if (begin == null) {
                begin = Long.valueOf(currentTimeMonthYear);
            }
            if (end != null) {
                // now discount end initialized by first day of month. Have to
                // change to last day of the same month
                Calendar discountEndCalendar = Calendar.getInstance();
                discountEndCalendar.setTimeInMillis(end.longValue());
                // set next month (first day)
                discountEndCalendar.add(Calendar.MONTH, 1);
                end = Long.valueOf(discountEndCalendar.getTimeInMillis());
                // return to needed month last day
                end = Long.valueOf(end.longValue() - 1);
                long discountEndTimeMonthYear = getTimeInMillisForFirstDay(end
                        .longValue());

                // end discount can not be less then current date
                if (currentTimeMonthYear > discountEndTimeMonthYear) {
                    addMessage(COMPONENT_THRU_ID, FacesMessage.SEVERITY_ERROR,
                            ERROR_DISCOUNT_DATE_FUTURE);
                    return OUTCOME_ERROR;
                }

                if (begin.longValue() > end.longValue()) {
                    addMessage(COMPONENT_THRU_ID, FacesMessage.SEVERITY_ERROR,
                            ERROR_DISCOUNT_DATE_BEFORE);
                    return OUTCOME_ERROR;
                }
            }

            voDiscount.setStartTime(begin);
            voDiscount.setEndTime(end);
        }

        VOOrganization org = getAccountingService().updateCustomerDiscount(
                voOrganization);
        if (customerUdaRows != null && !customerUdaRows.isEmpty()) {
            // only save the udas if there are some to avoid unnecessary calls
            List<VOUda> toSave = new ArrayList<VOUda>();
            for (UdaRow row : customerUdaRows) {
                VOUda uda = row.getUda();
                uda.setTargetObjectKey(org.getKey());
                // filter the list of UDAs so that only editable UDAs
                // (UdaConfigurationType.SUPPLIER) will passed to the service
                if (uda.getUdaDefinition().getConfigurationType()
                        .equals(UdaConfigurationType.SUPPLIER)) {
                    toSave.add(uda);
                }
            }
            getAccountingService().saveUdas(toSave);
            // reset to null for refresh on reload
            customerUdaRows = null;
        }
        selectedCustomer.setVOOrganization(voOrganization);
        // update in a list of customers
        updateSelectedCustomer(selectedCustomer.getOrganizationId(), org);

        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_ORGANIZATION_UPDATED,
                org.getOrganizationId());

        return OUTCOME_SUCCESS;
    }

    /**
     * Getting millisecond of the first day in month.
     * 
     * @param timeInMilis
     *            Time of any day of month.
     * @return First millisecond of month.
     */
    private long getTimeInMillisForFirstDay(long timeInMilis) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(timeInMilis);

        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentCalendar.set(Calendar.MINUTE, 0);
        currentCalendar.set(Calendar.SECOND, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);

        return currentCalendar.getTimeInMillis();
    }

    public List<UdaRow> getOrganizationUdas() {
        if (customerUdaRows == null) {
            Organization selectedCustomer = getSelectedCustomer();
            if (selectedCustomer != null) {
                long key = selectedCustomer.getVOOrganization().getKey();
                try {
                    customerUdaRows = udaBean.getCustomerUdas(key);
                } catch (ObjectNotFoundException | ValidationException
                        | OrganizationAuthoritiesException
                        | OperationNotPermittedException e) {
                    ExceptionHandler.execute(e);
                }
            } else {
                List<VOUdaDefinition> customerUdaDefinitions = udaBean
                        .getForType(UdaBean.CUSTOMER);
                return UdaRow.getUdaRows(customerUdaDefinitions,
                        new ArrayList<VOUda>());
            }
        }
        return customerUdaRows;
    }

    public ImageUploader getImageUploader() {
        return imageUploader;
    }

    public boolean isConfirmed() {
        // the flag is reset by every read access
        if (confirmed) {
            confirmed = false;
            return true;
        }
        return false;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isShowConfirm() {
        return showConfirm;
    }

    public String getInstanceIdsForSuppliers() {
        if (instanceIdsForSuppliers == null)
            return "";
        StringBuffer buf = new StringBuffer();
        for (String instanceId : instanceIdsForSuppliers) {
            if (buf.length() > 0) {
                buf.append("\n");
            }
            buf.append(instanceId);
        }
        return buf.toString();
    }

    public boolean isCustomerOrganization() {
        final User u = getCurrentUser();
        return !u.isSupplier() && !u.isTechnologyProvider() && !u.isReseller()
                && !u.isBroker();
    }

    public boolean isLdapManaged() {
        return ldapManaged;
    }

    public void setLdapManaged(boolean defineLdapManaged) {
        this.ldapManaged = defineLdapManaged;
    }

    public boolean isInternalAuthMode() {
        internalAuthMode = getApplicationBean().isInternalAuthMode();
        return internalAuthMode;
    }

    public void setInternalAuthMode(boolean internalAuthMode) {
        this.internalAuthMode = internalAuthMode;
    }

    ApplicationBean getApplicationBean() {

        if (appBean == null) {
            appBean = ui.findBean(APPLICATION_BEAN);
        }

        return appBean;
    }

}
