/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.landingpage;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;

/**
 * @author zankov
 * 
 */
@Remote
public interface EnterpriseLandingpageService {

    /**
     * Retrieves all services and subscriptions of the firts three categories.
     * <p>
     * Required role: none
     * 
     * @param marketplaceId
     *            the ID of the marketplace
     * @param locale
     *            the language in which to retrieve the categories. Specify a
     *            language code as returned by <code>getLanguage()</code> of
     *            <code>java.util.Locale</code>.
     * 
     * @return response a <code>Response</code> object containing
     *         POLandingpageResult that imply three Lists of landingpage entries
     *         (List<POLandingpageEntry>).
     */
    public Response loadLandingpageEntries(String marketplaceId, String locale);
}
