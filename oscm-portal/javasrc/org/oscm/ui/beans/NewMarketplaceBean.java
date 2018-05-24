/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Mar 15, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.ui.common.ExceptionActionListener;
import org.oscm.ui.model.NewMarketplace;

/**
 * The bean responsible for providing model and action for creating a new
 * marketplace. Representing the controller for the MVC pattern.
 * 
 * @author tang
 * 
 */
@ViewScoped
@ManagedBean(name="newMarketplaceBean")
public class NewMarketplaceBean extends BaseBean {

    NewMarketplace model;
    
    @ManagedProperty(value="#{menuBean}")
    MenuBean menuBean;
   

    /**
     * @return the menuBean
     */
    public MenuBean getMenuBean() {
        return menuBean;
    }

    /**
     * @param menuBean
     *            the menuBean to set
     */
    public void setMenuBean(MenuBean menuBean) {
        this.menuBean = menuBean;
    }
    
    /**
     * Returns the model to use for creating a new marketplace. If no model is
     * set, it will be initialized with the default values.
     * 
     * @return the {@link NewMarketplace} model
     */
    public NewMarketplace getModel() {
        if (model == null) {
            // just create - default values are initialized on construction
            model = new NewMarketplace();
        }
        return model;
    }

    /**
     * Model to value object conversion. If <code>null</code> is passed,
     * <code>null</code> will be returned.
     * 
     * @param nmp
     *            the {@link NewMarketplace} model to convert to a
     *            {@link VOMarketplace}
     * @return the created {@link VOMarketplace} or <code>null</code>
     */
    VOMarketplace toVOMarketplace(NewMarketplace nmp) {
        if (nmp == null) {
            return null;
        }
        VOMarketplace vmp = new VOMarketplace();
        vmp.setName(nmp.getName());
        vmp.setOpen(!nmp.isClosed());
        vmp.setOwningOrganizationId(nmp.getOwningOrganizationId());
        vmp.setTaggingEnabled(nmp.isTaggingEnabled());
        vmp.setReviewEnabled(nmp.isReviewEnabled());
        vmp.setCategoriesEnabled(nmp.isCategoriesEnabled());
        vmp.setTenantId(nmp.getTenantId());
        vmp.setMarketplaceId(nmp.getId());
        return vmp;
    }

    /**
     * Checks token validity and creates a new marketplace based on the provided
     * model. The model data will be converted to the {@link VOMarketplace} that
     * is required for the service call. On successful creation, the menu
     * visibility will be reset.
     * 
     * @return the logical outcome - <code>null</code> here as we stay on view
     * @throws SaaSApplicationException
     *             thrown on creation, handled by the
     *             {@link ExceptionActionListener}
     */
    public String createMarketplace() throws SaaSApplicationException {
        if (isTokenValid()) {
            VOMarketplace marketplace = getMarketplaceService()
                    .createMarketplace(toVOMarketplace(model));
            addMessage(null, FacesMessage.SEVERITY_INFO,
                    INFO_MARKETPLACE_CREATED, marketplace.getMarketplaceId());
            resetToken();
            menuBean.resetMenuVisibility();
            model = null;
        }
        return null;
    }

}
