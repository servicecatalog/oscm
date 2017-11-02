/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.08.2011                                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.dialog.mp.serviceDetails.ServiceDetailsModel;
import org.oscm.ui.model.Service;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOService;

/**
 * Backing bean for service deactivation and reactivation.
 */
@ManagedBean
@ViewScoped
public class ServiceActivationBean extends BaseBean implements Serializable {

    /**
     * 
     */
    private static final String CLOSE_MODAL = "CLOSE_MODAL_WINDOW_ATTR";

    /**
     * 
     */
    private static final long serialVersionUID = 8992302747122954498L;

    private String suspensionReason;

    @ManagedProperty(value = "#{serviceDetailsModel}")
    private ServiceDetailsModel serviceDetailsModel;
    private VOService service;
    private Service serviceWrapper;
    private Boolean marketplaceOwnerRightsCache;

    /**
     * Needed for Messages_*.property files.
     */
    private static final String STATUS_PREFIX = ServiceStatus.class
            .getSimpleName() + ".";

    /**
     * Suspends a service.
     * 
     * @return the updated service
     */
    public String suspendService() {
        Service selectedService = serviceDetailsModel.getSelectedService();
        if (selectedService != null) {
            try {
                setService(getProvisioningService().suspendService(
                        selectedService.getVO(), suspensionReason));
            } catch (SaaSApplicationException ex) {
                ExceptionHandler.execute(ex);
                FacesContext.getCurrentInstance().getAttributes()
                        .put(CLOSE_MODAL, Boolean.TRUE);
                return OUTCOME_ERROR;
            }
        }
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_SERVICE_SUSPENDED);
        suspensionReason = null;
        serviceDetailsModel.setSelectedServiceKey(serviceDetailsModel
                .getSelectedService().getKey() + "");

        return OUTCOME_SUCCESS;
    }

    public String getErrorPanel() {
        boolean closeModal = FacesContext.getCurrentInstance().getAttributes()
                .get(CLOSE_MODAL) != null;
        if (closeModal) {
            return ":globalMessagesHolder";
        }
        return ":deactivateServicePanelmodalErrorMasterPanel";
    }

    /**
     * Resumes a service.
     * 
     * @return the updated service
     */
    public String resumeService() {

        Service selectedService = serviceDetailsModel.getSelectedService();
        if (selectedService != null) {
            try {
                setService(getProvisioningService().resumeService(
                        selectedService.getVO()));
            } catch (SaaSApplicationException ex) {
                ExceptionHandler.execute(ex);
                return OUTCOME_ERROR;
            }
        }
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_SERVICE_RESUMED);
        serviceDetailsModel.setSelectedServiceKey(serviceDetailsModel
                .getSelectedService().getKey() + "");

        return OUTCOME_SUCCESS;
    }

    /**
     * Returns if the service is suspended or not.
     * 
     * @return Returns true, if the service is suspended, otherwise false.
     */
    public boolean isServiceSuspended() {
        VOService sel_service = getService();
        if (sel_service == null) {
            return false;
        }
        return sel_service.getStatus().equals(ServiceStatus.SUSPENDED);
    }

    /**
     * Returns if the service is active or not.
     * 
     * @return Returns true, if the service is suspended, otherwise false.
     */
    public boolean isServiceActive() {
        VOService sel_service = getService();
        if (sel_service == null) {
            return false;
        }
        return sel_service.getStatus().equals(ServiceStatus.ACTIVE);
    }

    /**
     * Returns if the deactivate link should be shown or not.
     * 
     * @return Returns true, if the service is active and the current user is
     *         the marketplace owner, otherwise false.
     */
    public boolean isDeactivationLinkVisible() {
        return isServiceActive() && isMarketplaceOwner();
    }

    /**
     * Returns if the reactivate link should be shown or not.
     * 
     * @return Returns true, if the service is suspended and the current user is
     *         the marketplace owner, otherwise false.
     */
    public boolean isReactivationLinkVisible() {
        return isServiceSuspended() && isMarketplaceOwner();
    }

    /**
     * Used for the UI enablement of the components which can be used to change
     * the service status.
     * 
     * @return <code>true</code> if the components should be shown, otherwise
     *         <code>false</code>.
     */
    public boolean isServiceStatusChangeAllowed() {
        // Since bug 8796, the mpl owner role is cached in this bean (request
        // scope + keepalive)
        if (marketplaceOwnerRightsCache == null) {
            marketplaceOwnerRightsCache = new Boolean(
                    super.isMarketplaceOwner());
        }
        return marketplaceOwnerRightsCache.booleanValue();
    }

    /**
     * Returns the service status.
     * 
     * @return the service status
     */
    public String getServiceStatus() {

        VOService sel_service = getService();
        if (sel_service == null) {
            return "";
        }
        return sel_service.getStatus().name();
    }

    /**
     * Gets the suspension reason.
     * 
     * @return the suspension reason
     */
    public String getSuspensionReason() {
        return suspensionReason;
    }

    /**
     * Sets the suspension reason.
     * 
     * @param suspensionReason
     *            the new suspension reason
     */
    public void setSuspensionReason(String suspensionReason) {
        this.suspensionReason = suspensionReason;
    }

    /**
     * Sets the service and the service wrapper.
     * 
     * @param service
     *            the new service instance
     */
    private void setService(VOService service) {
        this.service = service;
        setServiceWrapper(service);
    }

    /**
     * Returns the selected service from the subscription bean.
     * 
     * @return Returns the selected service.
     */
    private VOService getService() {
        if (service != null) {
            return service;
        }
        Service selectedService = serviceDetailsModel.getSelectedService();
        if (selectedService != null) {
            setService(selectedService.getVO());
        }
        return service;
    }

    /**
     * Sets the service wrapper.
     * 
     * @param service
     *            the new VOService instances
     */
    private void setServiceWrapper(VOService service) {
        serviceWrapper = new Service(service);
    }

    /**
     * Returns a service wrapper with the selected service instance.
     * 
     * @return Returns the selected service.
     */
    public Service getServiceWrapper() {
        return serviceWrapper;
    }

    /**
     * Returns the status of the selected service in the following format :
     * ServiceStatus.ACTIVE or ServiceStatus.SUSPENDED. Needed to visualize the
     * status of the service in serviceDetails.jsf.
     * 
     * @return the service status
     */
    public String getStatusKey() {
        VOService sel_service = getService();
        if (sel_service == null) {
            return "";
        }
        return STATUS_PREFIX + sel_service.getStatus().name();
    }

    /**
     * @return the serviceDetailsModel
     */
    public ServiceDetailsModel getServiceDetailsModel() {
        return serviceDetailsModel;
    }

    /**
     * @param serviceDetailsModel
     *            the serviceDetailsModel to set
     */
    public void setServiceDetailsModel(ServiceDetailsModel serviceDetailsModel) {
        this.serviceDetailsModel = serviceDetailsModel;
    }

}
