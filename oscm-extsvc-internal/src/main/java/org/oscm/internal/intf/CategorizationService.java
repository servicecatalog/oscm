/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2012-02-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOService;

/**
 * Remote interface of the categorization service.
 * 
 */
@Remote
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

    public List<VOCategory> getCategories(String marketplaceId, String locale);

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

    public void saveCategories(List<VOCategory> toBeSaved,
            List<VOCategory> toBeDeleted, String locale)
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

    public List<VOService> getServicesForCategory(long categoryKey);

    /**
     * Check if any of the category is updated or not
     * 
     * @param categories
     *            categories to be checked
     * @throws ConcurrentModificationException
     *             throws if category has been updated
     * @throws ObjectNotFoundException
     *             throws if category does not exist
     */
    public void verifyCategoriesUpdated(List<VOCategory> categories)
            throws ConcurrentModificationException, ObjectNotFoundException;
}
