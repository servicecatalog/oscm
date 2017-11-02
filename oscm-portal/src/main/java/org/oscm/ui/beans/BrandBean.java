/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.apache.poi.util.IOUtils;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.RequestUrlHandler;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOMarketplace;

/**
 * The bean for the customization of the branding package.
 * 
 * @author farmaki
 * 
 */
@ViewScoped
@ManagedBean(name="brandBean")
public class BrandBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 2856016433312624579L;

    protected static final String ERROR_FETCH_BRANDING_PACKAGE = "shop.customizeBrand.fetchBrandingPackage.error";
    protected static final String ERROR_DOWNLOAD_BRANDING_PACKAGE = "shop.customizeBrand.downloadBrandingPackage.error";

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(BrandBean.class);

    @ManagedProperty(value="#{marketplaceBean}")
    private MarketplaceBean marketplaceBean;

    private String brandingUrl;
    private VOMarketplace marketplace;

    private byte[] brandingPackage;

    public MarketplaceBean getMarketplaceBean() {
        return marketplaceBean;
    }

    public void setMarketplaceBean(MarketplaceBean marketplaceBean) {
        this.marketplaceBean = marketplaceBean;
    }

    public boolean isMarketplaceSelected() {
        return marketplace != null;
    }

    public String getBrandingUrl() {
        return brandingUrl;
    }

    public void setBrandingUrl(String brandingUrl) {
        this.brandingUrl = brandingUrl;
    }

    protected String getWhiteLabelBrandingUrl() {
        return getFacesContext().getExternalContext().getRequestContextPath()
                + "/marketplace/css/mp.css";
    }

    /**
     * Marketplace chooser
     * 
     * @param event
     */
    public void processValueChange(ValueChangeEvent event) {
        String selectedMarketplaceId = (String) event.getNewValue();
        this.marketplaceBean.processValueChange(event);
        if (selectedMarketplaceId.equals("0")) {
            marketplace = null;
            setBrandingUrl(null);
        } else {
            try {
                marketplace = getMarketplaceService().getMarketplaceById(
                        selectedMarketplaceId);
                setBrandingUrl(getMarketplaceService().getBrandingUrl(
                        selectedMarketplaceId));
            } catch (ObjectNotFoundException e) {
                getMarketplaceBean().checkMarketplaceDropdownAndMenuVisibility(
                        null);
                marketplace = null;
                setBrandingUrl(null);
            }
        }
    }

    public void downloadBrandingPackage() throws IOException {
        
        if (brandingPackage == null) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_DOWNLOAD_BRANDING_PACKAGE);
            logger.logError(LogMessageIdentifier.ERROR_EXECUTE_DOWNLOAD_BRANDING_PACKAGE_WITH_NULL_DATA);
            
            return;
        }

        writeContentToResponse(brandingPackage, "branding-package.zip",
                "application/zip");

        brandingPackage = null;
    }

    public void fetchBrandingPackage() throws IOException {
        
        InputStream in = null;
        try {
            FacesContext fc = getFacesContext();
            in = fc.getExternalContext().getResourceAsStream(
                    "/WEB-INF/branding-package.zip");
            if (in == null) {
                addMessage(null, FacesMessage.SEVERITY_ERROR,
                        ERROR_FETCH_BRANDING_PACKAGE);
                logger.logError(LogMessageIdentifier.ERROR_FETCH_BRANDING_PACKAGE_RETURN_NULL);
                
                return;
            }
            byte[] bytes = IOUtils.toByteArray(in);
            brandingPackage = bytes;
            if (brandingPackage == null) {
                addMessage(null, FacesMessage.SEVERITY_ERROR,
                        ERROR_FETCH_BRANDING_PACKAGE);
                logger.logError(LogMessageIdentifier.ERROR_FETCH_BRANDING_PACKAGE_RETURN_NULL);
                
                return;
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public String validateCurrentUrl() {
        boolean isUrlAccessible = false;
        try {
            isUrlAccessible = RequestUrlHandler.isUrlAccessible(getBrandingUrl());
            addMessage(null, isUrlAccessible ? FacesMessage.SEVERITY_INFO
                    : FacesMessage.SEVERITY_ERROR,
                    isUrlAccessible ? INFO_CSS_CONNECTION_SUCCESS
                            : ERROR_CSS_CONNECTION);
        } catch (IOException e) {
            addMessage(null, FacesMessage.SEVERITY_ERROR, ERROR_CSS_CONNECTION);
        }
        return "";

    }

    /**
     * Checks if branding package data is available.
     * 
     * @return <code>true</code> if branding package data is available otherwise
     *         <code>false</code>.
     */
    public boolean isBrandingPackageAvailable() {
        return brandingPackage != null;
    }

    public void saveBrandingUrl() {
        
        try {
            // Call the marketplace service method for saving the URL.
            getMarketplaceService().saveBrandingUrl(marketplace, brandingUrl);
            // refresh the marketplace, to avoid concurrency exception
            marketplace = getMarketplaceService().getMarketplaceById(
                    marketplace.getMarketplaceId());
            // add success message
            String message = (brandingUrl != null && brandingUrl.trim()
                    .length() > 0) ? INFO_BRANDING_URL_SET
                    : INFO_WHITE_LABEL_BRANDING_URL_SET;
            addMessage(null, FacesMessage.SEVERITY_INFO, message);
        } catch (ObjectNotFoundException e) {
            getMarketplaceBean()
                    .checkMarketplaceDropdownAndMenuVisibility(null);
            marketplace = null;
            setBrandingUrl(null);
            ExceptionHandler.execute(e, true);
            return;
        } catch (SaaSApplicationException e) {
            ExceptionHandler.execute(e);
            return;
        }
    }

}
