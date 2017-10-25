/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 08.09.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.util.List;

import org.oscm.domobjects.Event;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * Assembler to handle VOTechnicalService <=> TechnicalProduct conversions.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class TechnicalProductAssembler extends BaseAssembler {

    public static final String FIELD_NAME_TECHNICAL_SERVICE_ID = "technicalServiceId";

    public static final String FIELD_NAME_TECHNICAL_SERVICE_BUILD_ID = "technicalServiceBuildId";

    public static final String FIELD_NAME_BASE_URL = "baseUrl";

    public static final String FIELD_NAME_PROVISIONING_URL = "provisioningUrl";

    public static final String FIELD_NAME_PROVISIONING_VERSION = "provisioningVersion";

    public static final String FIELD_NAME_LOGIN_PATH = "loginPath";

    private static final String PROTOCOLL_HTTP = "http://";
    private static final String PROTOCOLL_HTTPS = "https://";

    /**
     * Creates a value object representing the current settings for the
     * technical product. It also assembles the parameter definition
     * information.
     * 
     * @param tProd
     *            The technical product to be represented as value object.
     * @param platformParamterDefinitions
     *            The parameter definitions of type
     *            {@link ParameterType#PLATFORM_PARAMETER} that have to be
     *            included.
     * @param platformEvents
     *            The events supported by the platform.
     * @param facade
     *            The localizer facade object.
     * @param excludeNonConfigurableParameterDefinitions
     *            Indicates whether non-configurable parameters should be
     *            contained in the value object or not.
     * @return A value object representation of the given technical product.
     */

    public static VOTechnicalService toVOTechnicalProduct(
            TechnicalProduct tProd,
            List<ParameterDefinition> platformParamterDefinitions,
            List<Event> platformEvents, LocalizerFacade facade,
            boolean excludeNonConfigurableParameterDefinitions) {
        return toVOTechnicalProduct(tProd, platformParamterDefinitions,
                platformEvents, facade,
                excludeNonConfigurableParameterDefinitions,
                PerformanceHint.ALL_FIELDS);
    }

    public static VOTechnicalService toVOTechnicalProduct(
            TechnicalProduct tProd,
            List<ParameterDefinition> platformParamterDefinitions,
            List<Event> platformEvents, LocalizerFacade facade,
            boolean excludeNonConfigurableParameterDefinitions,
            PerformanceHint scope) {
        VOTechnicalService result = new VOTechnicalService();
        result.setTechnicalServiceId(tProd.getTechnicalProductId());
        if (scope != PerformanceHint.ONLY_IDENTIFYING_FIELDS) {
            result.setTechnicalServiceBuildId(tProd
                    .getTechnicalProductBuildId());
            result.setAccessType(tProd.getAccessType());
            result.setBaseUrl(tProd.getBaseURL());
            result.setLoginPath(tProd.getLoginPath());
            result.setProvisioningUrl(tProd.getProvisioningURL());
            result.setProvisioningVersion(tProd.getProvisioningVersion());
            result.setTechnicalServiceDescription(facade.getText(
                    tProd.getKey(),
                    LocalizedObjectTypes.TEC_PRODUCT_TECHNICAL_DESC));
            result.setLicense(facade.getText(tProd.getKey(),
                    LocalizedObjectTypes.PRODUCT_LICENSE_DESC));
            result.setAccessInfo(facade.getText(tProd.getKey(),
                    LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
            result.setParameterDefinitions(ParameterDefinitionAssembler
                    .toVOParameterDefinitions(platformParamterDefinitions,
                            tProd.getParameterDefinitions(),
                            excludeNonConfigurableParameterDefinitions, facade));
            result.setEventDefinitions(EventAssembler.toVOEventDefinitions(
                    platformEvents, tProd.getEvents(), facade));
            result.setRoleDefinitions(RoleAssembler.toVORoleDefinitions(
                    tProd.getRoleDefinitions(), facade));
            result.setTechnicalServiceOperations(TechnicalProductOperationAssembler
                    .toVOTechnicalServiceOperations(
                            tProd.getTechnicalProductOperations(), facade));
            result.setTags(TagAssembler.toStrings(tProd.getTags(),
                    facade.getLocale()));
            result.setBillingIdentifier(tProd.getBillingIdentifier());
            result.setExternalBilling(tProd.isExternalBilling());
            updateValueObject(result, tProd);
        }
        return result;
    }

    /**
     * Updates the fields in the TechnicalProduct object to reflect the changes
     * performed in the value object.
     * 
     * @param domObj
     *            The domain object to be updated.
     * @param voOrganization
     *            The value object.
     * @return The updated domain object.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     * @throws ConcurrentModificationException
     */
    public static TechnicalProduct updateTechnicalProduct(
            TechnicalProduct domObj, VOTechnicalService vo)
            throws ValidationException, ConcurrentModificationException {
        verifyVersionAndKey(domObj, vo);
        copyAttributes(domObj, vo);
        return domObj;
    }

    public static TechnicalProduct toTechnicalProduct(VOTechnicalService vo)
            throws ValidationException {
        final TechnicalProduct product = new TechnicalProduct();
        copyAttributes(product, vo);
        return product;
    }

    private static void copyAttributes(TechnicalProduct domObj,
            VOTechnicalService vo) throws ValidationException {

        BLValidator.isId(FIELD_NAME_TECHNICAL_SERVICE_ID,
                vo.getTechnicalServiceId(), true);
        BLValidator.isId(FIELD_NAME_TECHNICAL_SERVICE_BUILD_ID,
                vo.getTechnicalServiceBuildId(), false);
        BLValidator.isDescription(FIELD_NAME_LOGIN_PATH, vo.getLoginPath(),
                false);
        BLValidator.isDescription(FIELD_NAME_PROVISIONING_VERSION,
                vo.getProvisioningVersion(), false);

        String baseUrl = vo.getBaseUrl();
        while (baseUrl != null && (baseUrl.endsWith("\\"))) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        BLValidator.isUrl(
                FIELD_NAME_BASE_URL,
                baseUrl,
                vo.getAccessType() != ServiceAccessType.DIRECT
                        && vo.getAccessType() != ServiceAccessType.USER);

        String provisioningUrl = vo.getProvisioningUrl();
        if (vo.getAccessType() != ServiceAccessType.EXTERNAL) {
            // if the provisioning URL is relative and the base URL is set
            // create an absolute URL with the help of the base URL
            if (provisioningUrl != null) {
                String lower = provisioningUrl.toLowerCase();
                if (baseUrl != null && !lower.startsWith(PROTOCOLL_HTTP)
                        && !lower.startsWith(PROTOCOLL_HTTPS)) {
                    provisioningUrl = removeEndingSlash(baseUrl)
                            + provisioningUrl;
                }
            }
            BLValidator.isUrl(FIELD_NAME_PROVISIONING_URL, provisioningUrl,
                    true);
        } else if (provisioningUrl == null) {
            provisioningUrl = "";
        }

        String loginPath = vo.getLoginPath();
        if (loginPath == null) {
            loginPath = "";
        }

        BLValidator.isRelativeUrl(FIELD_NAME_LOGIN_PATH, loginPath, baseUrl,
                false);

        domObj.setTechnicalProductId(vo.getTechnicalServiceId());
        domObj.setTechnicalProductBuildId(vo.getTechnicalServiceBuildId());
        domObj.setAccessType(vo.getAccessType());
        domObj.setBaseURL(baseUrl);
        domObj.setLoginPath(loginPath);
        domObj.setProvisioningURL(provisioningUrl);
        domObj.setProvisioningVersion(vo.getProvisioningVersion());
        domObj.setBillingIdentifier(vo.getBillingIdentifier());
    }

    private static String removeEndingSlash(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

}
