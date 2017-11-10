/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 10.09.2009                                                      
 *                                                                              
 *  Completion Time: <date>                                     
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.beans;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.Part;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.PartHandler;
import org.oscm.ui.model.OperationParameterRow;
import org.oscm.ui.model.OperationRow;
import org.oscm.ui.model.ParameterRow;
import org.oscm.ui.model.TechnicalService;
import org.oscm.internal.billingadapter.BillingAdapterService;
import org.oscm.internal.billingadapter.POBaseBillingAdapter;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.ImportException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * Backing bean for technical service related actions
 * 
 */
@ViewScoped
@ManagedBean(name="techServiceBean")
public class TechServiceBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -6589444143426805719L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(TechServiceBean.class);

    List<VOTechnicalService> technicalServices;

    private TechnicalService newTechnicalService;

    TechnicalService selectedTechnicalService;

    private boolean selectedTechnicalServiceActive;

    private Part uploadedFile;

    List<TechnicalService> selectableTechnicalServices;

    private List<ParameterRow> parametersAndOptions = null;

    private List<OperationRow> operationsAndParameters = null;

    private SaaSApplicationException applicationException;
    
    @ManagedProperty(value="#{menuBean}")
    private MenuBean menuBean;
    
    @ManagedProperty(value="#{sessionBean}")
    private SessionBean sessionBean;
    
    BillingAdapterService billingAdapterService;
    
    byte[] buf;

    public List<VOTechnicalService> getTechnicalServices() {
        if (technicalServices == null) {
            try {
                technicalServices = getProvisioningService()
                        .getTechnicalServices(
                                OrganizationRoleType.TECHNOLOGY_PROVIDER);
            } catch (OrganizationAuthoritiesException e) {
                ExceptionHandler.execute(e);
            }
        }
        return technicalServices;
    }
    
    public String getDefaultBillingIdentifier() {
        Response response = getBillingAdapterService()
                .getDefaultBaseBillingAdapter();
    
        return response
                .getResult(POBaseBillingAdapter.class).getBillingIdentifier();
    }
    
    private BillingAdapterService getBillingAdapterService() {
        if (billingAdapterService == null) {
            billingAdapterService = ui.findService(BillingAdapterService.class);
        }
        return billingAdapterService;
    }

    public void setSelectedTechnicalServiceKeyReadonly(
            long selectedTechnicalServiceKey) {
        // will do nothing to avoid the reload of the whole service list on
        // submit like with the not readonly method
    }

    public long getSelectedTechnicalServiceKeyReadonly() {
        return getSelectedTechnicalServiceKey();
    }

    public long getSelectedTechnicalServiceKey() {
        initPreselectedTechnicalService();
        if (selectedTechnicalService != null) {
            return selectedTechnicalService.getKey();
        }
        return 0;
    }

    public void setSelectedTechnicalServiceKey(long selectedTechnicalServiceKey) {
        selectedTechnicalService = null;
        selectedTechnicalServiceActive = false;
        parametersAndOptions = null;
        operationsAndParameters = null;
        sessionBean.setSelectedTechnicalServiceKey(0);
        if (getTechnicalServices() == null) {
            return;
        }
        for (VOTechnicalService techService : getTechnicalServices()) {
            if (techService.getKey() == selectedTechnicalServiceKey) {
                selectedTechnicalService = new TechnicalService(techService);
                sessionBean
                        .setSelectedTechnicalServiceKey(techService.getKey());
                // we don't want to display the platform events and parameter
                // definitions in the detail panel
                for (Iterator<VOEventDefinition> it = techService
                        .getEventDefinitions().iterator(); it.hasNext();) {
                    if (it.next().getEventType() == EventType.PLATFORM_EVENT) {
                        it.remove();
                    }
                }
                for (Iterator<VOParameterDefinition> it = techService
                        .getParameterDefinitions().iterator(); it.hasNext();) {
                    if (it.next().getParameterType() == ParameterType.PLATFORM_PARAMETER) {
                        it.remove();
                    }
                }
                break;
            }
        }
    }

    public void setSelectedTechnicalServiceKeyWithValidation(
            long selectedTechnicalServiceKey) {
        setSelectedTechnicalServiceKey(selectedTechnicalServiceKey);
        if (selectedTechnicalService != null) {
            try {
                getProvisioningService().validateTechnicalServiceCommunication(
                        selectedTechnicalService.getVo());
                selectedTechnicalServiceActive = true;
            } catch (Exception e) {
                e.printStackTrace();
                Throwable th = e;
                while(th != null && !(th instanceof SaaSApplicationException)) {
                    th = th.getCause();
                }
                applicationException = (SaaSApplicationException) th;
            }
        }
    }

    public void setSelectedTechnicalServiceKeyWithExceptionAndRefresh(
            long selectedTechnicalServiceKey) {
        setSelectedTechnicalServiceKey(selectedTechnicalServiceKey);
        if (selectedTechnicalService != null) {
            try {
                getProvisioningService().validateTechnicalServiceCommunication(
                        selectedTechnicalService.getVo());
                selectedTechnicalServiceActive = true;
            } catch (SaaSApplicationException e) {
                this.technicalServices = null;
                sessionBean.setSelectedTechnicalServiceKey(0);
                this.selectedTechnicalService = null;
                ui.handleException(e, true);
            }
        }
    }

    public TechnicalService getSelectedTechnicalService() {
        initPreselectedTechnicalService();
        return selectedTechnicalService;
    }

    /**
     * If the {@link #sessionBean} contains a valid technical service key and no
     * technical service object is currently set, it will be initialized.
     */
    private void initPreselectedTechnicalService() {
        long key = sessionBean.getSelectedTechnicalServiceKey();
        if (selectedTechnicalService == null && key > 0) {
            setSelectedTechnicalServiceKey(key);
        }
    }

    public boolean isSelectedTechnicalServiceActive() {
        return selectedTechnicalServiceActive;
    }

    /**
     * Create a new technical service object which can be used to set the
     * attributes of a new technical service definition.
     * 
     * @return the new technical service object
     */
    public TechnicalService getNewTechnicalService() {
        if (newTechnicalService == null) {
            VOTechnicalService voTechService = new VOTechnicalService();
            voTechService.setBillingIdentifier(getDefaultBillingIdentifier());
            newTechnicalService = new TechnicalService(voTechService);
        }
        return newTechnicalService;
    }

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(final MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    /**
     * Empty action.
     * 
     * @return null.
     */
    public String apply() {
        return null;
    }

    /**
     * Creates a new technical service without events, parameters and
     * localizable attributes.
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     *             Thrown from the business logic.
     */
    public String create() throws SaaSApplicationException {

        if (newTechnicalService != null) {
            newTechnicalService.setBillingIdentifier(getDefaultBillingIdentifier());
            VOTechnicalService created = getProvisioningService()
                    .createTechnicalService(newTechnicalService.getVo());
            sessionBean.setSelectedTechnicalServiceKey(created.getKey());
            addMessage(
                    null,
                    FacesMessage.SEVERITY_INFO,
                    INFO_TECH_SERVICE_CREATED,
                    new Object[] { newTechnicalService.getTechnicalServiceId() });
            // don't display the attributes again
            newTechnicalService = null;
            // ensure reload of created technical service on edit page
            technicalServices = null;
            // help the navigation to highlight the correct navigation item
            menuBean.setCurrentPageLink(MenuBean.LINK_TECHSERVICE_EDIT);
        }

        return OUTCOME_SUCCESS;
    }

    /**
     * Import an XML file with technical service definitions
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     *             Thrown from the business logic.
     */
    public String xmlImport() throws SaaSApplicationException {
        
        if (PartHandler.isEmpty(uploadedFile)) {
            addMessage(null, FacesMessage.SEVERITY_ERROR, ERROR_EMPTY_FILE);
            return OUTCOME_ERROR;
        }
        
        try {          
            byte[] buffer = PartHandler.getBuffer(uploadedFile);
            getProvisioningService().importTechnicalServices(buffer);

            selectedTechnicalService = null;
            technicalServices = null;
            addMessage(null, FacesMessage.SEVERITY_INFO,
                    INFO_TECH_SERVICE_IMPORTED);
        } catch (IOException e) {
            ImportException ex = new ImportException(e.getLocalizedMessage());
            logger.logError(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.ERROR_IMPORT_XML_FAILED);
            throw ex;
        }

        return OUTCOME_SUCCESS;
    }

    /**
     * Delete the selected technical service.
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     */
    public String delete() throws SaaSApplicationException {

        if (isTokenValid()) {
            if (selectedTechnicalService != null) {
                try {
                    getProvisioningService().deleteTechnicalService(
                            selectedTechnicalService.getVo());
                    sessionBean.setSelectedTechnicalServiceKey(0);
                    addMessage(null, FacesMessage.SEVERITY_INFO,
                            INFO_TECH_SERVICE_DELETED,
                            new Object[] { selectedTechnicalService
                                    .getTechnicalServiceId() });
                } finally {
                    selectedTechnicalService = null;
                    technicalServices = null;
                }
            }
            resetToken();
        }

        return OUTCOME_SUCCESS;
    }

    public List<TechnicalService> getSelectableTechnicalServices() {
        if (selectableTechnicalServices == null) {
            selectableTechnicalServices = new ArrayList<>();
            List<VOTechnicalService> tps = getTechnicalServices();
            if (tps != null) {
                for (VOTechnicalService tp : tps) {
                    selectableTechnicalServices.add(new TechnicalService(tp));
                }
            }
        }
        return selectableTechnicalServices;
    }

    public String exportTechnicalServices() throws SaaSApplicationException {

        List<VOTechnicalService> toExport = new ArrayList<>();
        List<TechnicalService> list = getSelectableTechnicalServices();
        for (TechnicalService tp : list) {
            if (tp.isSelected()) {
                toExport.add(tp.getVo());
            }
        }
        buf = null;
        try {
            buf = getProvisioningService().exportTechnicalServices(toExport);
        } catch (SaaSApplicationException e) {
            selectableTechnicalServices = null;
            allServicesSelected = false;
            throw e;
        }
        if (buf == null) {
            return OUTCOME_ERROR;
        }

        return OUTCOME_SUCCESS;
    }

    public boolean isDataAvailable() {
        return buf != null;
    }

    public String showData() throws IOException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        String filename = sdf.format(Calendar.getInstance().getTime())
                + "_TechnicalServices.xml";
        String contentType = "text/xml";
        writeContentToResponse(buf, filename, contentType);

        return OUTCOME_SUCCESS;
    }

    public List<ParameterRow> getParameters() {
        TechnicalService service = getSelectedTechnicalService();
        if (service == null) {
            parametersAndOptions = null;
            return Collections.emptyList();
        }
        List<VOParameterDefinition> list = service.getParameterDefinitions();
        if (list == null) {
            parametersAndOptions = null;
            return Collections.emptyList();
        }
        if (parametersAndOptions == null) {
            parametersAndOptions = new ArrayList<>();
            for (VOParameterDefinition def : list) {
                parametersAndOptions.add(new ParameterRow(def, null));
                List<VOParameterOption> options = def.getParameterOptions();
                for (VOParameterOption opt : options) {
                    parametersAndOptions.add(new ParameterRow(def, opt));
                }
            }
        }
        return parametersAndOptions;
    }

    public List<VOEventDefinition> getEvents() {
        TechnicalService service = getSelectedTechnicalService();
        if (service == null) {
            return Collections.emptyList();
        }
        List<VOEventDefinition> list = service.getEventDefinitions();
        if (list == null) {
            list = Collections.emptyList();
        }
        return list;
    }

    public List<VORoleDefinition> getRoles() {
        TechnicalService service = getSelectedTechnicalService();
        if (service == null) {
            return Collections.emptyList();
        }
        List<VORoleDefinition> list = service.getRoleDefinitions();
        if (list == null) {
            list = Collections.emptyList();
        }
        return list;
    }

    public List<OperationRow> getOperations() {
        TechnicalService service = getSelectedTechnicalService();
        if (service == null) {
            operationsAndParameters = null;
            return Collections.emptyList();
        }
        List<VOTechnicalServiceOperation> list = service
                .getTechnicalServiceOperations();
        if (operationsAndParameters == null) {
            operationsAndParameters = new ArrayList<>();
            for (VOTechnicalServiceOperation vo : list) {
                operationsAndParameters.add(new OperationRow(vo));
                List<VOServiceOperationParameter> paras = vo
                        .getOperationParameters();
                if (paras == null) {
                    continue;
                }
                for (VOServiceOperationParameter vop : paras) {
                    operationsAndParameters.add(new OperationParameterRow(vo,
                            vop));
                }
            }
        }
        return operationsAndParameters;
    }

    public boolean isCheckingAccessInfoEmpty() {
        boolean isValidationAccessInfo = false;
        if (selectedTechnicalService != null) {
            String locale = getUserLanguage();
            ServiceAccessType accessType = selectedTechnicalService.getVo()
                    .getAccessType();
            if (locale.equals("en")
                    && (accessType == ServiceAccessType.DIRECT || accessType == ServiceAccessType.USER)) {
                isValidationAccessInfo = true;
            }
        }
        return isValidationAccessInfo;
    }

    public String save() throws SaaSApplicationException {
        if (selectedTechnicalService == null) {
            return OUTCOME_ERROR;
        }
        try {
            getProvisioningService().saveTechnicalServiceLocalization(
                    selectedTechnicalService.getVo());
            notifyTagCloudBean();
            addMessage(null, FacesMessage.SEVERITY_INFO,
                    INFO_TECH_SERVICE_SAVED,
                    new Object[] { selectedTechnicalService
                            .getTechnicalServiceId() });
        } finally {
            technicalServices = null;
        }
        setSelectedTechnicalServiceKey(selectedTechnicalService.getKey());
        return OUTCOME_SUCCESS;
    }

    /**
     * reset categories for marketplace so that fresh data is loaded if
     * currently logged in user wants to preview his changes.
     */
    private void notifyTagCloudBean() {
        ui.findTagCloudBean().resetTagsForMarketplace();
    }

    public SaaSApplicationException getApplicationException() {
        return applicationException;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public boolean getBaseUrlVisible() {
        return !(!newTechnicalService.existAccessType()
                || newTechnicalService.isAccessTypeDirect()
                || newTechnicalService.isAccessTypeSaml());
    }

    public boolean getProvisioningUrlVisible() {
        return !(!newTechnicalService.existAccessType()
                || newTechnicalService.isAccessTypeExternal());
    }

    public boolean getLoginPathVisible() {
        return !(!newTechnicalService.existAccessType()
                || newTechnicalService.isAccessTypeDirect()
                || newTechnicalService.isAccessTypeSaml()
                || newTechnicalService.isAccessTypeExternal());
    }

    public boolean getAccessInfoVisible() {
        return newTechnicalService.isAccessTypeDirect()
                || newTechnicalService.isAccessTypeSaml();
    }

    /*
     * value change listener for accessType selectOneMenu
     */
    public void accessTypeChanged(ValueChangeEvent event) {
        ServiceAccessType newAccessType = ServiceAccessType.valueOf(event
                .getNewValue().toString());
        this.newTechnicalService.setAccessType(newAccessType);
    }
    
    public boolean isExportEnabled() {
        List<TechnicalService> list = getSelectableTechnicalServices();
        if (list != null) {
            for (TechnicalService ts : list) {
                if (ts.isSelected())
                    return true;
            }
        }
        return false;
    }

    boolean allServicesSelected;

    public void setAllServicesSelected(boolean selected) {
        allServicesSelected = selected;
        List<TechnicalService> list = getSelectableTechnicalServices();
        for (TechnicalService ts : list) {
            ts.setSelected(selected);
        }
    }

    public boolean isAllServicesSelected() {
        return allServicesSelected;
    }
    
    public void setNewTechnicalService(TechnicalService newTechnicalService) {
        this.newTechnicalService = newTechnicalService;
    }
}
