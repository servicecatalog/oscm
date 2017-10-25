/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2011-07-15                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.types.exceptions.InvalidPhraseException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.vo.ListCriteria;
import org.oscm.vo.VOServiceListResult;

/**
 * Remote interface providing the functionality to search for entities.
 * 
 */
@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
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
    @WebMethod
    public VOServiceListResult getServicesByCriteria(
            @WebParam(name = "marketplaceId") String marketplaceId,
            @WebParam(name = "locale") String locale,
            @WebParam(name = "listCriteria") ListCriteria listCriteria)
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
    @WebMethod
    public VOServiceListResult searchServices(
            @WebParam(name = "marketplaceId") String marketplaceId,
            @WebParam(name = "locale") String locale,
            @WebParam(name = "searchPhrase") String searchPhrase)
            throws InvalidPhraseException, ObjectNotFoundException;
}
