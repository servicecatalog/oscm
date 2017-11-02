/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-9-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.trackingCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.internal.components.POMarketplace;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.trackingCode.POTrackingCode;
import org.oscm.internal.trackingCode.TrackingCodeManagementService;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * @author Zou
 * 
 */
public class TrackingCodeCtrl extends BaseBean implements Serializable {

    private static final long serialVersionUID = 3413229840008848612L;

    TrackingCodeManagementService trackingCodeManagementService;

    private TrackingCodeModel model;

    static final String INFO_SAVED = "info.trackingcode.saved";
    static final String CONCURRENT_MODIFICATION_ERROR = "concurrentModification";

    private SessionBean sessionBean;

    /**
     * initializer method called by <adm:initialize />
     * 
     * @return empty string (due to value jsf binding )
     * 
     *         workaround: to be refactored under jsf 2.0
     * 
     */
    public String getInitializeTrackingCode() {
        initializeModel();
        return "";
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    void initSelectableMarketplaces() {
        List<POMarketplace> marketplaces = getTrackingCodeManagementService()
                .getMarketplaceSelections();
        List<SelectItem> uiMarketplaces = initMarketplaceSelector(marketplaces);
        model.setMarketplaces(uiMarketplaces);
    }

    TrackingCodeManagementService getTrackingCodeManagementService() {
        if (trackingCodeManagementService == null) {
            trackingCodeManagementService = sl
                    .findService(TrackingCodeManagementService.class);
        }
        return trackingCodeManagementService;
    }

    private List<SelectItem> initMarketplaceSelector(List<POMarketplace> mpls) {
        List<SelectItem> uiMarketplaces = new ArrayList<SelectItem>();
        for (POMarketplace mp : mpls) {
            uiMarketplaces.add(new SelectItem(mp.getMarketplaceId(), mp
                    .getDisplayName()));
        }
        return uiMarketplaces;
    }

    void initializeModel() {
        if (!model.isInitialized()) {
            initSelectableMarketplaces();
            initializeTrackingCode();
            model.setInitialized(true);
        }
    }

    void initializeTrackingCode() {
        model.setTrackingCodeObject(new POTrackingCode());
    }

    /**
     * @param model
     *            the model to set
     */
    public void setModel(TrackingCodeModel model) {
        this.model = model;
    }

    /**
     * @return the model
     */
    public TrackingCodeModel getModel() {
        return model;
    }

    /**
     * action method for save button
     * 
     * @return null: stay on same page
     */
    public String save() {
        String outcome = null;
        try {
            Response response = getTrackingCodeManagementService()
                    .saveTrackingCode(model.getTrackingCodeObject());
            ui.handle(INFO_SAVED);
            updateModelTrackingCode(response.getResult(POTrackingCode.class));
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
            outcome = CONCURRENT_MODIFICATION_ERROR;
        }
        return outcome;
    }

    private void updateModelTrackingCode(POTrackingCode result) {
        model.setTrackingCodeObject(result);
    }

    boolean loadTrackingCode(String marketplaceId) {
        try {
            Response r = getTrackingCodeManagementService()
                    .loadTrackingCodeForMarketplace(marketplaceId);
            POTrackingCode trackingCode = r.getResult(POTrackingCode.class);
            model.setTrackingCodeObject(trackingCode);
            model.setSelectedMarketplace(marketplaceId);
            return true;
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
            model.setTrackingCodeObject(new POTrackingCode());
            model.setSelectedMarketplace(null);
            initSelectableMarketplaces();
            return false;
        }
    }

    /**
     * value change listener for marketplace chooser
     */
    public String marketplaceChanged() {
        String result = "";
        String selectedMarketplaceId = model.getSelectedMarketplace();
        if (selectedMarketplaceId == null || selectedMarketplaceId.equals("0")) {
            model.setTrackingCodeObject(new POTrackingCode());
            model.setSelectedMarketplace(null);
        } else {
            if (!loadTrackingCode(selectedMarketplaceId)) {
                result = CONCURRENT_MODIFICATION_ERROR;
            }
        }
        return result;
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

    /**
     * if no marketplace is selected disable all other input fields and buttons
     */
    public boolean isFieldsDisabled() {
        if (model == null || model.getSelectedMarketplace() == null
                || model.getSelectedMarketplace().length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getTrackingCodeForCurrentMarketplace() {
        String marketplaceTrackingCode = getSessionBean()
                .getMarketplaceTrackingCode();
        if (marketplaceTrackingCode == null) {
            if (loadTrackingCode(getMarketplaceId())) {
                marketplaceTrackingCode = model.getTrackingCodeValue();
            }
            if (marketplaceTrackingCode == null) {
                marketplaceTrackingCode = "";
            }
            getSessionBean()
                    .setMarketplaceTrackingCode(marketplaceTrackingCode);
        }
        return marketplaceTrackingCode;
    }
}
