/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Enes Sejfi                      
 *                                                                              
 *  Creation Date: 11.06.2012                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.assembler;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.PublicLandingpage;
import org.oscm.landingpageService.local.VOPublicLandingpage;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Assembler to handle VOLandingpage <=> Landingpage conversions.
 */
public class LandingpageAssembler extends BaseAssembler {

	private static final Log4jLogger logger = LoggerFactory
			.getLogger(LandingpageAssembler.class);

	/**
	 * Creates a value object representing the current settings for the
	 * landingpage.
	 * 
	 * @param domObj
	 *            The technical landingpage to be represented as value object.
	 * @return A value object representation of the given landingpage.
	 */
	public static VOPublicLandingpage toVOLandingpage(PublicLandingpage domObj) {
		if (domObj == null) {
			return null;
		}

		VOPublicLandingpage voObj = new VOPublicLandingpage();
		updateValueObject(voObj, domObj);
		voObj.setMarketplaceId(domObj.getMarketplace().getMarketplaceId());
		voObj.setNumberServices(domObj.getNumberServices());
		voObj.setFillinCriterion(domObj.getFillinCriterion());

		// LandingpageProductAssembler is NOT called because of cyclic depency
		// between projects 'oscm-marketplaceservice-intsvc' and
		// 'oscm-serviceprovisioning-intsvc'. Please call the
		// LandingpageProductAssembler in LandingpageServiceBean!

		return voObj;
	}

	/**
	 * Updates the fields in the Landingpage object to reflect the changes
	 * performed in the value object.
	 * 
	 * @param domObj
	 *            The domain object to be updated.
	 * @param voObj
	 *            The value object.
	 * @return The updated domain object.
	 * @throws ValidationException
	 *             Thrown if the validation of the value objects failed.
	 * @throws ConcurrentModificationException
	 *             Thrown if the object versions do not match.
	 * @throws ValidationException
	 *             Thrown if the attributes to copy at the value object do not
	 *             meet all constraints.
	 */
	public static PublicLandingpage updateLandingpage(PublicLandingpage domObj,
			VOPublicLandingpage voObj) throws ValidationException,
			ConcurrentModificationException {
		if (domObj == null || voObj == null) {
			IllegalArgumentException e = new IllegalArgumentException(
					"Parameters must not be null");
			logger.logError(Log4jLogger.SYSTEM_LOG, e,
					LogMessageIdentifier.ERROR_PARAMETER_NULL);
			throw e;
		}
		if (domObj.getKey() != 0) {
			verifyVersionAndKey(domObj, voObj);
		}
		validate(voObj);
		copyAttributes(domObj, voObj);
		return domObj;
	}

	/**
	 * Creates a new landing page domain object based on the given value object.
	 * 
	 * @param voObj
	 *            The value object.
	 * @return The new domain object
	 * @throws ValidationException
	 */
	public static PublicLandingpage toLandingpage(VOPublicLandingpage voObj)
			throws ValidationException {
		final PublicLandingpage domObj = new PublicLandingpage();
		validate(voObj);
		copyAttributes(domObj, voObj);
		return domObj;
	}

	/**
	 * Validates the given Landingpage object that reflects the settings in the
	 * given value object.
	 * 
	 * @param voObj
	 *            The value object containing the values to be validated.
	 * @throws ValidationException
	 *             Thrown if the attributes at the value object do not meet all
	 *             constraints.
	 */
	public static void validate(VOPublicLandingpage voObj)
			throws ValidationException {
		BLValidator.isNotNull("marketplaceId", voObj.getMarketplaceId());
		BLValidator.isNonNegativeNumber("numberServices",
				voObj.getNumberServices());
		BLValidator.isNotNull("fillinCriterion", voObj.getFillinCriterion());
		BLValidator.isNotNull("landingpageServices",
				voObj.getLandingpageServices());
	}

	/**
	 * Copies all attributes in Landingpage object according to the values
	 * specified in the value object.
	 * 
	 * @param domObj
	 *            The domain object from which the attributed of the value
	 *            object should be copied in.
	 * @param voObj
	 *            The value object containing the values to be copied.
	 */
	private static void copyAttributes(PublicLandingpage domObj,
			VOPublicLandingpage voObj) {
		domObj.setNumberServices(voObj.getNumberServices());
		domObj.setFillinCriterion(voObj.getFillinCriterion());
	}
}
