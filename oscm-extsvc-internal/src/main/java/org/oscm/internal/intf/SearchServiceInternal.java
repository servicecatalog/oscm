/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                    
 *                                                                              
 *  Creation Date: 19.10.2011                                                      
 *                                                                              
 *  Completion Time: 19.10.2011                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.InvalidPhraseException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.ListCriteria;
import org.oscm.internal.vo.VOServiceListResult;

/**
 * Remote interface providing the functionality to search for entities. This
 * interface contains methods that provide performance optimized access. It is
 * intended only as a temporary optimization and will be replaced by other
 * means. Only for internal usage.
 * 
 */
@Remote
public interface SearchServiceInternal {

    /**
     * Returns a list of services according to the specified criteria. If the
     * calling user is logged in, the list only includes services which are
     * visible to him and his organization. Otherwise, the list only includes
     * services visible to anonymous (non-registered) users.
     * <p>
     * Required role: none
     * 
     * @param marketplaceId
     *            the ID of the marketplace to get the services for
     * @param locale
     *            the language in which the service data are to be returned.
     *            Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     * @param listCriteria
     *            a <code>ListCriteria</code> object specifying the search and
     *            sorting conditions as well as list and page sizes for the
     *            results
     * @param performanceHint
     *            a <code>performanceHint</code> constant specifying the data to
     *            include in the result. This can be used to increase the search
     *            performance.
     * @return the list of services matching the specified criteria
     * @throws ObjectNotFoundException
     *             if the specified marketplace or category was not found
     */
    public VOServiceListResult getServicesByCriteria(String marketplaceId,
            String locale, ListCriteria listCriteria,
            PerformanceHint performanceHint) throws ObjectNotFoundException;

    /**
     * Returns a list of services according to the specified criteria. The list
     * all services which are for his organization. This method is a temporary
     * included to give the unit administrators the possibility to assign and
     * deassign accessible services to their units.
     * <p>
     * Required role: none
     * 
     * @param marketplaceId
     *            the ID of the marketplace to get the services for
     * @param locale
     *            the language in which the service data are to be returned.
     *            Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     * @param listCriteria
     *            a <code>ListCriteria</code> object specifying the search and
     *            sorting conditions as well as list and page sizes for the
     *            results
     * @param performanceHint
     *            a <code>performanceHint</code> constant specifying the data to
     *            include in the result. This can be used to increase the search
     *            performance.
     * @return the list of services matching the specified criteria
     * @throws ObjectNotFoundException
     *             if the specified marketplace or category was not found
     */
    public VOServiceListResult getAccesibleServices(
            String marketplaceId, String locale, ListCriteria listCriteria,
            PerformanceHint performanceHint) throws ObjectNotFoundException;

    /**
     * Executes a full-text search for services with the specified search
     * phrase. If the calling user is logged in, the search result only includes
     * services which are visible to him and his organization. Otherwise, the
     * list only includes services visible to anonymous (non-registered) users.
     * <p>
     * The search is carried out in the services' name, tags, short description,
     * description, and price model description. This attribute sequence
     * determines the ranking of the services in the search result. This means
     * that services whose name contains the search phrase have a higher rank
     * than services where the search phrase is found in the tags or a
     * description.
     * <p>
     * Multiple terms in the search phrase are combined by a logical AND. This
     * means that all of the terms must be found for a service to be included in
     * the results.
     * <p>
     * Required role: none
     * 
     * @param marketplaceId
     *            the ID of the marketplace to get the services for
     * @param locale
     *            the language in which the service data are to be returned.
     *            Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     * @param searchPhrase
     *            the phrase to search the services for * @param performanceHint
     *            a <code>performanceHint</code> constant specifying the data to
     *            include in the result. This can be used to increase the search
     *            performance.
     * @return the list of services found by the specified phrase
     * @throws InvalidPhraseException
     *             if the underlying search engine fails to parse the generated
     *             query for the search phrase
     * @throws ObjectNotFoundException
     *             if the specified marketplace or category was not found
     */
    public VOServiceListResult searchServices(String marketplaceId,
            String locale, String searchPhrase, PerformanceHint performanceHint)
            throws InvalidPhraseException, ObjectNotFoundException;
}
