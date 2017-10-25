/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.portallandingpage;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOService;

@Remote
public interface LandingpageService {
    /**
     * Returns the list of services displayed on the landing page of the given
     * marketplace. The landing page configuration defines the maximum number of
     * services as well as a criterion according to which the page is filled up
     * if not enough featured services are available.
     * <p>
     * If the calling user is logged in, the list only includes services which
     * are visible to him and his organization. Otherwise, the list only
     * includes services visible to anonymous (non-registered) users.
     * <p>
     * Required role: none
     * 
     * @param marketplaceId
     *            the ID of the marketplace
     * @param locale
     *            the language in which service details are to be returned.
     *            Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     * @return the list of services
     */
    public List<VOService> servicesForLandingpage(String marketplaceId,
            String locale) throws ObjectNotFoundException;
    
    
}
