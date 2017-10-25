/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.local;

import javax.ejb.Local;

import org.oscm.domobjects.Product;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOPriceModelLocalization;
import org.oscm.internal.vo.VOServiceLocalization;

@Local
public interface ServiceProvisioningServiceLocalizationLocal {

    /**
     * Checks if the current caller is allowed to change the localization of the
     * marketable service with the given key;
     * 
     * @param serviceKey
     * @return
     * @throws ObjectNotFoundException
     */
    public boolean checkIsAllowedForLocalizingService(final long serviceKey)
            throws ObjectNotFoundException;

    /**
     * Returns the localized texts for a marketable service.
     * <p>
     * Required role: none
     * 
     * @param service
     *            the service to get the localized texts for
     * @return a <code>VOServiceLocalization</code> object with the localized
     *         texts
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have read access to the service
     */
    public VOServiceLocalization getServiceLocalization(Product service)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Saves localized texts of a marketable service.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service
     * 
     * @param serviceKey
     *            the service key for which the localized texts are to be saved
     * @param localization
     *            a <code>VOServiceLocalization</code> object with the localized
     *            texts to save
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have write access to the service
     * @throws ValidationException
     *             if one of the localized texts is longer than 100 bytes
     * @throws ConcurrentModificationException
     *             if the stored service or texts are changed by another user in
     *             the time between reading and writing them
     */
    public void saveServiceLocalization(long serviceKey,
            VOServiceLocalization localization) throws ObjectNotFoundException,
            OperationNotPermittedException, ValidationException,
            ConcurrentModificationException;

    /**
     * Returns the localized texts for a price model.
     * <p>
     * Required role: none
     * 
     * @param serviceKey
     *            the serviceKey to get the localized texts for
     * @return a <code>VOPriceModelLocalization</code> object with the localized
     *         texts
     * @throws ObjectNotFoundException
     *             if the price model is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have read access to the price
     *             model
     */
    public VOPriceModelLocalization getPriceModelLocalization(long serviceKey)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Saves the localized texts for a price model.
     * <p>
     * Required role: reseller organization that owns the price model
     * 
     * @param serviceKey
     *            the key of the service for which the localized texts are to be
     *            saved
     * @param true if the price model is chargeable, false otherwise
     * @param localization
     *            a <code>VOPriceModelLocalization</code> object with the
     *            localized texts to save
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have write access to the price
     *             model
     * @throws ConcurrentModificationException
     *             if the stored price model or texts are changed by another
     *             user in the time between reading and writing them
     */
    public void savePriceModelLocalizationForReseller(long serviceKey,
            boolean isChargable, VOPriceModelLocalization localization)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException;

    /**
     * Saves the localized texts for a price model.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * price model
     * 
     * @param priceModelKey
     *            the key of the price model for which the localized texts are
     *            to be saved
     * @param true if the price model is chargeable, false otherwise
     * @param localization
     *            a <code>VOPriceModelLocalization</code> object with the
     *            localized texts to save
     * @throws ObjectNotFoundException
     *             if the price model is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have write access to the price
     *             model
     * @throws ConcurrentModificationException
     *             if the stored price model or texts are changed by another
     *             user in the time between reading and writing them
     */
    public void savePriceModelLocalizationForSupplier(long priceModelKey,
            boolean isChargable, VOPriceModelLocalization localization)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException;
}
