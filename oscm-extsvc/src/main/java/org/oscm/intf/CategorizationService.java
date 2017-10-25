/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2012-02-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOCategory;
import org.oscm.vo.VOService;

/**
 * Remote interface of the categorization service.
 * 
 */
@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface CategorizationService {

    /**
     * Retrieves all categories of the given marketplace and language.
     * <p>
     * Required role: none
     * 
     * @param marketplaceId
     *            the ID of the marketplace
     * @param locale
     *            the language in which to retrieve the categories. Specify a
     *            language code as returned by <code>getLanguage()</code> of
     *            <code>java.util.Locale</code>.
     * @return the list of categories
     */
    @WebMethod
    public List<VOCategory> getCategories(
            @WebParam(name = "marketplaceId") String marketplaceId,
            @WebParam(name = "locale") String locale);

    /**
     * Adds, modifies, and deletes categories for the given marketplace and
     * language. The categories which are to be added or modified are specified
     * in the <code>toBeSaved</code> parameter, the ones to be removed in the
     * <code>toBeDeleted</code> parameter.
     * <p>
     * If any of the save or delete operations fails, no changes at all are
     * committed to the database.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * 
     * @param toBeSaved
     *            the categories to be stored. New categories (with a numeric
     *            key of 0) are added, existing ones are updated.
     * @param toBeDeleted
     *            the categories to be deleted. Only the identifier of the
     *            categories is evaluated.
     * @param locale
     *            the language for which the categories in the
     *            <code>toBeSaved</code> list are to be stored. Specify a
     *            language code as returned by <code>getLanguage()</code> of
     *            <code>java.util.Locale</code>.
     * @throws ObjectNotFoundException
     *             if the marketplace is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws ConcurrentModificationException
     *             if the data stored for the given categories is changed by
     *             another user in the time between reading and writing it
     * @throws ValidationException
     *             if a category is invalid
     * @throws NonUniqueBusinessKeyException
     *             if a category to be created already exists
     */
    @WebMethod
    public void saveCategories(
            @WebParam(name = "toBeSaved") List<VOCategory> toBeSaved,
            @WebParam(name = "toBeDeleted") List<VOCategory> toBeDeleted,
            @WebParam(name = "locale") String locale)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, ValidationException,
            NonUniqueBusinessKeyException;

    /**
     * Returns all services to which the given category is assigned.
     * <p>
     * Required role: none
     * 
     * @param categoryKey
     *            the numeric key of the category for which the services are to
     *            be returned
     * @return the list of services
     */
    @WebMethod
    public List<VOService> getServicesForCategory(
            @WebParam(name = "categoryKey") long categoryKey);
}
