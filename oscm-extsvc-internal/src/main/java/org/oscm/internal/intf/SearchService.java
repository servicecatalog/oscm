/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2011-07-15                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.InvalidPhraseException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.ListCriteria;
import org.oscm.internal.vo.VOServiceListResult;

/**
 * Remote interface providing the functionality to search for entities.
 * 
 */
@Remote
public interface SearchService {

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
     *            results.
     * @throws ObjectNotFoundException
     *             if the given marketplace or a category or tag specified in
     *             the <code>ListCriteria</code> object is not found
     * @return the list of services matching the specified criteria.
     */

    public VOServiceListResult getServicesByCriteria(String marketplaceId,
            String locale, ListCriteria listCriteria)
            throws ObjectNotFoundException;

    /**
     * Executes a full-text search for services with the specified search
     * phrase. If the calling user is logged in, the search result only includes
     * services which are visible to him and his organization. Otherwise, the
     * list only includes services visible to anonymous (non-registered) users.
     * <p>
     * The search is carried out in the services' name, tags, categories, short
     * description, description, and price model description. This attribute
     * sequence determines the ranking of the services in the search result.
     * This means that services whose name contains the search phrase have a
     * higher rank than services where the search phrase is found in the tags or
     * a description.
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
     *            the phrase to search the services for
     * @return the list of services found by the specified phrase
     * @throws InvalidPhraseException
     *             if the underlying search engine fails to parse the generated
     *             query for the search phrase
     * @throws ObjectNotFoundException
     *             if the specified marketplace or a relevant category is not
     *             found
     */

    public VOServiceListResult searchServices(String marketplaceId,
            String locale, String searchPhrase)
            throws InvalidPhraseException, ObjectNotFoundException;

    /**
     * (Re)Creates the initial index from scratch for the objects already being
     * in the database. The indexing operation is performed only if either the
     * index is empty (which will be the case when the server is started for the
     * very first time), and skipped otherwise, or if <code>force</code> is set
     * to <code>true</code> (in this case, a possibly existing index is
     * overridden).
     */

    public void initIndexForFulltextSearch(boolean force);
}
