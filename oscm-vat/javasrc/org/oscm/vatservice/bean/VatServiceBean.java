/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock
 *                                                                              
 *  Creation Date: 17.11.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vatservice.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.SupportedCountry;
import org.oscm.domobjects.VatRate;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.vatservice.assembler.VatRateAssembler;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.intf.VatService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.BaseVO;
import org.oscm.internal.vo.VOCountryVatRate;
import org.oscm.internal.vo.VOOrganizationVatRate;
import org.oscm.internal.vo.VOVatRate;

/**
 * Session Bean implementation to handle VAT-related tasks.
 * 
 * @author pock
 * 
 */
@Stateless
@Remote(VatService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class VatServiceBean implements VatService {

    private final static Log4jLogger logger = LoggerFactory
            .getLogger(VatServiceBean.class);

    @Resource
    private SessionContext sessionCtx;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    private LocalizerServiceLocal localizer;

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    public List<VOCountryVatRate> getCountryVats() {
        
        Organization organization = dm.getCurrentUser().getOrganization();
        List<VOCountryVatRate> result = new ArrayList<VOCountryVatRate>();
        for (VatRate vatRate : organization.getDefinedVatRates()) {
            if (vatRate.getTargetCountry() != null) {
                result.add(VatRateAssembler.toVOCountryVatRate(vatRate));
            }
        }
        
        return result;
    }

    public VOVatRate getDefaultVat() {
        
        Organization organization = dm.getCurrentUser().getOrganization();
        if (organization.getDefinedVatRates().isEmpty()) {
            
            return null;
        }
        for (VatRate vatRate : organization.getDefinedVatRates()) {
            if (vatRate.getTargetCountry() == null
                    && vatRate.getTargetOrganization() == null) {
                
                return VatRateAssembler.toVOVatRate(vatRate);
            }
        }
        SaaSSystemException se = new SaaSSystemException(
                "Default VAT rate is missing!");
        logger.logError(Log4jLogger.SYSTEM_LOG, se,
                LogMessageIdentifier.ERROR_DEFAULT_VAT_RARE_MISSING);
        throw se;
    }

    public List<VOOrganizationVatRate> getOrganizationVats() {
        
        Organization organization = dm.getCurrentUser().getOrganization();
        LocalizerFacade lf = new LocalizerFacade(localizer, dm.getCurrentUser()
                .getLocale());

        List<VOOrganizationVatRate> result = new ArrayList<VOOrganizationVatRate>();
        for (VatRate vatRate : organization.getDefinedVatRates()) {
            if (vatRate.getTargetOrganization() != null) {
                result.add(VatRateAssembler
                        .toVOOrganizationVatRate(vatRate, lf));
            }
        }
        
        return result;
    }

    public boolean getVatSupport() {
        return !dm.getCurrentUser().getOrganization().getDefinedVatRates()
                .isEmpty();
    }

    @RolesAllowed("SERVICE_MANAGER")
    public void saveAllVats(VOVatRate defaultVat,
            List<VOCountryVatRate> countryVats,
            List<VOOrganizationVatRate> organizationVats)
            throws OrganizationAuthoritiesException,
            ConcurrentModificationException, OperationNotPermittedException,
            ValidationException {
        Organization organization = dm.getCurrentUser().getOrganization();

        if (defaultVat == null) {
            if (countryVats != null && countryVats.size() >= 0
                    || organizationVats != null && organizationVats.size() >= 0) {
                ValidationException e = new ValidationException(
                        ReasonEnum.VAT_NOT_SUPPORTED, null, null);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_VAT_NOT_SUPPORTED,
                        organization.getOrganizationId());
                throw e;
            }
            for (VatRate vatRate : organization.getDefinedVatRates()) {
                dm.remove(vatRate);
            }
        } else {
            saveDefaultVat(defaultVat);
            saveCountryVats(countryVats);
            saveOrganizationVats(organizationVats);
        }

    }

    @RolesAllowed("SERVICE_MANAGER")
    public void saveCountryVats(List<VOCountryVatRate> countryVats)
            throws OrganizationAuthoritiesException,
            ConcurrentModificationException, ValidationException {
        
        Organization organization = dm.getCurrentUser().getOrganization();
        validateVatSupportEnabled(organization);

        // get a map with all existing country vat rates
        Map<String, VatRate> existing = new HashMap<String, VatRate>();
        for (VatRate vatRate : organization.getDefinedVatRates()) {
            if (vatRate.getTargetCountry() != null) {
                existing.put(vatRate.getTargetCountry().getCountryISOCode(),
                        vatRate);
            }
        }

        if (countryVats != null) {
            Set<String> processedCountries = new HashSet<>();

            for (VOCountryVatRate vo : countryVats) {
                if (vo == null || vo.getCountry() == null) {
                    sessionCtx.setRollbackOnly();
                    ValidationException e = new ValidationException(
                            ReasonEnum.REQUIRED,
                            VatRateAssembler.FIELD_NAME_COUNTRY,
                            new Object[] { VatRateAssembler.FIELD_NAME_COUNTRY });
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.WARN_VALIDATION_FAILED_FOR_MEMBER,
                            VatRateAssembler.FIELD_NAME_COUNTRY,
                            ReasonEnum.REQUIRED.name(),
                            VatRateAssembler.FIELD_NAME_COUNTRY);
                    throw e;
                }
                SupportedCountry sc = new SupportedCountry(vo.getCountry());
                sc = (SupportedCountry) dm.find(sc);
                if (sc == null) {
                    sessionCtx.setRollbackOnly();
                    ValidationException e = new ValidationException(
                            ReasonEnum.COUNTRY_NOT_SUPPORTED,
                            VatRateAssembler.FIELD_NAME_COUNTRY,
                            new Object[] { vo.getCountry() });
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.WARN_VALIDATION_FAILED_FOR_MEMBER,
                            VatRateAssembler.FIELD_NAME_COUNTRY,
                            ReasonEnum.COUNTRY_NOT_SUPPORTED.name(),
                            vo.getCountry());
                    throw e;
                }
                if (processedCountries.contains(vo.getCountry())) {
                    sessionCtx.setRollbackOnly();
                    ValidationException e = new ValidationException(
                            ReasonEnum.DUPLICATE_VALUE,
                            VatRateAssembler.FIELD_NAME_COUNTRY,
                            new Object[] { vo.getCountry() });
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.WARN_VALIDATION_FAILED_FOR_MEMBER,
                            VatRateAssembler.FIELD_NAME_COUNTRY,
                            ReasonEnum.DUPLICATE_VALUE.name(), vo.getCountry());
                    throw e;
                }
                processedCountries.add(vo.getCountry());

                VatRate vatRate = existing.remove(vo.getCountry());
                if (vatRate == null) {
                    if (vo.getRate() != null) {
                        verifyKeyEmpty(vo);
                        createVat(organization, vo, sc, null);
                    }
                } else {
                    if (vo.getRate() == null) {
                        BaseAssembler.verifyVersionAndKey(vatRate, vo);
                        dm.remove(vatRate);
                    } else {
                        VatRateAssembler.updateVatRate(vatRate, vo);
                    }
                }
            }
        }

        
    }

    @RolesAllowed("SERVICE_MANAGER")
    public void saveDefaultVat(VOVatRate defaultVat)
            throws OrganizationAuthoritiesException,
            ConcurrentModificationException, ValidationException {
        
        Organization organization = dm.getCurrentUser().getOrganization();
        if (defaultVat == null || defaultVat.getRate() == null) {
            for (VatRate vatRate : organization.getDefinedVatRates()) {
                dm.remove(vatRate);
            }
            
            return;
        }

        for (VatRate vatRate : organization.getDefinedVatRates()) {
            if (vatRate.getTargetCountry() == null
                    && vatRate.getTargetOrganization() == null) {
                VatRateAssembler.updateVatRate(vatRate, defaultVat);
                
                return;
            }
        }

        verifyKeyEmpty(defaultVat);
        createVat(organization, defaultVat, null, null);

    }

    @RolesAllowed("SERVICE_MANAGER")
    public void saveOrganizationVats(
            List<VOOrganizationVatRate> organizationVats)
            throws OrganizationAuthoritiesException,
            ConcurrentModificationException, OperationNotPermittedException,
            ValidationException {
        
        Organization organization = dm.getCurrentUser().getOrganization();
        validateVatSupportEnabled(organization);

        // get a map with all existing customer vat rates
        Map<String, VatRate> existing = new HashMap<String, VatRate>();
        for (VatRate vatRate : organization.getDefinedVatRates()) {
            if (vatRate.getTargetOrganization() != null) {
                existing.put(vatRate.getTargetOrganization()
                        .getOrganizationId(), vatRate);
            }
        }

        if (organizationVats != null) {
            // get a map with all existing customer
            Map<String, Organization> customers = new HashMap<String, Organization>();
            Query query = dm.createNamedQuery("Organization.getForSupplierKey");
            query.setParameter("supplierKey",
                    Long.valueOf(organization.getKey()));
            query.setParameter("referenceType",
                    OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
            for (Organization customer : ParameterizedTypes.iterable(
                    query.getResultList(), Organization.class)) {
                customers.put(customer.getOrganizationId(), customer);
            }
            Set<String> processedCustomers = new HashSet<String>();

            for (VOOrganizationVatRate vo : organizationVats) {
                if (vo == null || vo.getOrganization() == null
                        || vo.getOrganization().getOrganizationId() == null) {
                    sessionCtx.setRollbackOnly();
                    ValidationException e = new ValidationException(
                            ReasonEnum.REQUIRED,
                            VatRateAssembler.FIELD_NAME_ORGANIZATION,
                            new Object[] { VatRateAssembler.FIELD_NAME_ORGANIZATION });
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.WARN_VALIDATION_FAILED_FOR_MEMBER,
                            VatRateAssembler.FIELD_NAME_ORGANIZATION,
                            ReasonEnum.REQUIRED.name(),
                            VatRateAssembler.FIELD_NAME_ORGANIZATION);
                    throw e;
                }
                String custId = vo.getOrganization().getOrganizationId();
                Organization customer = customers.get(custId);
                if (customer == null) {
                    sessionCtx.setRollbackOnly();
                    OperationNotPermittedException e = new OperationNotPermittedException();
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.WARN_CUSTOMER_NOT_CURRENT_ORGANIZATION,
                            custId);
                    throw e;
                }
                if (processedCustomers.contains(vo.getOrganization()
                        .getOrganizationId())) {
                    sessionCtx.setRollbackOnly();
                    ValidationException e = new ValidationException(
                            ReasonEnum.DUPLICATE_VALUE,
                            VatRateAssembler.FIELD_NAME_ORGANIZATION,
                            new Object[] { vo.getOrganization()
                                    .getOrganizationId() });
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.WARN_VALIDATION_FAILED_FOR_MEMBER,
                            VatRateAssembler.FIELD_NAME_ORGANIZATION,
                            ReasonEnum.DUPLICATE_VALUE.name(), vo
                                    .getOrganization().getOrganizationId());
                    throw e;
                }
                processedCustomers.add(custId);

                VatRate vatRate = existing.remove(customer.getOrganizationId());
                if (vatRate == null) {
                    if (vo.getRate() != null) {
                        verifyKeyEmpty(vo);
                        createVat(organization, vo, null, customer);
                    }
                } else {
                    if (vo.getRate() == null) {
                        BaseAssembler.verifyVersionAndKey(vatRate, vo);
                        dm.remove(vatRate);
                    } else {
                        VatRateAssembler.updateVatRate(vatRate, vo);
                    }
                }
            }
        }
        
    }

    private void createVat(Organization owningOrganization, VOVatRate vo,
            SupportedCountry targetCountry, Organization targetOrganization)
            throws ValidationException, ConcurrentModificationException {
        VatRate vatRate = new VatRate();
        vatRate.setOwningOrganization(owningOrganization);
        vatRate.setTargetCountry(targetCountry);
        vatRate.setTargetOrganization(targetOrganization);
        VatRateAssembler.updateVatRate(vatRate, vo);
        try {
            dm.persist(vatRate);
            owningOrganization.getDefinedVatRates().add(vatRate);
        } catch (NonUniqueBusinessKeyException e) {
            // must not occur because VatRate doesn't have a business key
            SaaSSystemException se = new SaaSSystemException(
                    VatRate.class.getSimpleName()
                            + " with non unique business key!", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_NOT_UNIQUE_BUSINESS_KEY,
                    VatRate.class.getSimpleName());
            throw se;
        }
    }

    /**
     * Throws a validation exception if the VAT support is disabled for the
     * given organization.
     * 
     * 
     * @param organization
     *            The organization for which the VAT support is validated.
     */
    private void validateVatSupportEnabled(Organization organization)
            throws ValidationException {
        if (organization.getDefinedVatRates().isEmpty()) {
            ValidationException e = new ValidationException(
                    ReasonEnum.VAT_NOT_SUPPORTED, null, null);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_VAT_NOT_SUPPORTED);
            throw e;
        }
    }

    /**
     * Throws a concurrent modification exception if the key of the given value
     * object is set.
     * 
     * 
     * @param vo
     *            The value object to verify.
     */
    private void verifyKeyEmpty(BaseVO vo)
            throws ConcurrentModificationException {
        if (vo.getKey() > 0) {
            ConcurrentModificationException cme = new ConcurrentModificationException(
                    vo);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, cme,
                    LogMessageIdentifier.WARN_CONCURRENT_MODIFICATION);
            throw cme;
        }
    }

}
