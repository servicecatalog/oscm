/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2012-06-11                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.landingpageService.local;

import java.io.Serializable;

import org.oscm.internal.vo.BaseVO;
import org.oscm.internal.vo.VOService;

/**
 * Represents a featured service on the landing page (home page) of a
 * marketplace.
 */
public class VOLandingpageService extends BaseVO  {
    private static final long serialVersionUID = 2519753042468989864L;

    /**
     * The position of the service on the landing page.
     */
    private int position;

    /**
     * The actual marketable service represented by the featured service on the
     * landing page.
     */
    private VOService service;

    /**
     * Retrieves the position of the featured service in the list of featured
     * services on the landing page.
     * 
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the position of the featured service in the list of featured
     * services on the landing page.
     * 
     * @param position
     *            the position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Retrieves the marketable service that is represented by the featured
     * service on the landing page.
     * 
     * @return the marketable service
     */
    public VOService getService() {
        return service;
    }

    /**
     * Sets the marketable service to be represented by the featured service on
     * the landing page.
     * 
     * @param service
     *            the marketable service
     */
    public void setService(VOService service) {
        this.service = service;
    }
}
