/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.pricemodel.external;

import java.io.IOException;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;

import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;
import org.oscm.internal.pricemodel.external.ExternalPriceModelException;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;

/**
 * @author stavreva
 * 
 */
public abstract class ExternalPriceModelCtrl extends BaseBean {

    UiDelegate ui = new UiDelegate();

    @ManagedProperty(value = "#{externalPriceModelModel}")
    private ExternalPriceModelModel model;

    private ApplicationBean appBean;

    private SessionBean sessionBean;

    final String PRICEMODELBEAN = "priceModelBean";
    final String APPBEAN = "appBean";
    final String SESSION_BEAN = "sessionBean";

    public ExternalPriceModelModel getModel() {
        return model;
    }

    public void setModel(ExternalPriceModelModel model) {
        this.model = model;
    }

    public void setAppBean(ApplicationBean appBean) {
        this.appBean = appBean;
    }

    public ApplicationBean getAppBean() {
        return appBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    @PostConstruct
    public void initialize() {
        initBeans();
    }

    public void loadPriceModelContent(PriceModel priceModel)
            throws ExternalPriceModelException {
        Locale locale = JSFUtils.getViewLocale();

        PriceModelContent priceModelContent = getLocalizedPriceModelContent(
                locale, priceModel);

        model.setSelectedPriceModelContent(priceModelContent);
        model.setSelectedPriceModel(priceModel);
        model.setSelectedPriceModelId(priceModel.getId().toString());
    }

    protected void initBeans() {
        if (getAppBean() == null) {
            this.setAppBean((ApplicationBean) ui.findBean(APPBEAN));
        }
        if (getSessionBean() == null) {
            this.setSessionBean((SessionBean) ui.findBean(SESSION_BEAN));
        }
    }

    public void showPersistedPriceModel(VOService selectedService) {

        VOPriceModel priceModel = selectedService.getPriceModel();

        if (priceModel != null && priceModel.isExternal()) {
            PriceModelContent selectedPriceModelContent = new PriceModelContent(
                    priceModel.getPresentationDataType(),
                    priceModel.getPresentation());
            PriceModel selectedPriceModel = new PriceModel(priceModel.getUuid());
            getModel().setSelectedPriceModel(selectedPriceModel);
            getModel().setSelectedPriceModelContent(selectedPriceModelContent);
            getModel().setSelectedPriceModelId(priceModel.getUuid().toString());
            return;
        }
        resetPriceModel();
    }

    public void resetPriceModel() {
        getModel().setSelectedPriceModel(null);
        getModel().setSelectedPriceModelContent(null);
        getModel().setSelectedPriceModelId("");
    }

    private PriceModelContent getLocalizedPriceModelContent(Locale locale,
            PriceModel externalPriceModel) {
        PriceModelContent priceModelContent = externalPriceModel.get(locale);
        if (priceModelContent == null) {
            priceModelContent = externalPriceModel.get(getAppBean()
                    .getDefaultLocale());
        }
        return priceModelContent;
    }

    public void display() throws IOException, SaaSApplicationException {

        PriceModelContent priceModelContent = getModel()
                .getSelectedPriceModelContent();

        if (priceModelContent == null) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_EXTERNAL_PRICEMODEL_NOT_AVAILABLE);
            return;
        }
        ExternalPriceModelDisplayHandler displayHandler = new ExternalPriceModelDisplayHandler();
        displayHandler.setContent(priceModelContent.getContent());
        displayHandler.setContentType(priceModelContent.getContentType());
        displayHandler.setFilename(priceModelContent.getFilename());
        displayHandler.display();
        return;
    }

    public void reloadPriceModel(ServiceType serviceType, VOService service) {
        if (service.getServiceType() == serviceType) {
            showPersistedPriceModel(service);
            return;
        }
        resetPriceModel();
    }

    public void initPersistedPriceModel(ServiceType serviceType,
            VOService selectedService) {
        if (selectedService == null) {
            return;
        }
        if (selectedService.getServiceType() == serviceType) {
            showPersistedPriceModel(selectedService);
        } else {
            resetPriceModel();
        }
    }

}