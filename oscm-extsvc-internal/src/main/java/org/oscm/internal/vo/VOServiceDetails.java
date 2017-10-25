/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * Represents a marketable service, enhanced by details of the underlying
 * technical service.
 */
public class VOServiceDetails extends VOService {

    private static final long serialVersionUID = -7939154314507680269L;

    /**
     * The technical service this marketable service is based on.
     */
    private VOTechnicalService technicalService;

    private boolean imageDefined;

    /**
     * Retrieves the technical service on which the marketable service is based.
     * 
     * @return the technical service
     */
    public VOTechnicalService getTechnicalService() {
        return technicalService;
    }

    /**
     * Sets the technical service on which the marketable service is based.
     * 
     * @param technicalService
     *            the technical service
     */
    public void setTechnicalService(VOTechnicalService technicalService) {
        this.technicalService = technicalService;
    }

    /**
     * Checks whether an image was defined which is used when customers
     * subscribe to the service.
     * 
     * @return <code>true</code> if an image was defined, <code>false</code>
     *         otherwise
     */
    public boolean isImageDefined() {
        return imageDefined;
    }

    /**
     * Specifies whether an image is to be used when customers subscribe to the
     * service.
     * 
     * @param imageDefined
     *            <code>true</code> if an image is to be used,
     *            <code>false</code> otherwise
     */
    public void setImageDefined(boolean imageDefined) {
        this.imageDefined = imageDefined;
    }

}
