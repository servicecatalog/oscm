/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.data;

import java.util.Locale;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.SupportedCountry;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

public class SupportedCountries {

    /**
     * Returns the SupportedCountry for the given country code. The
     * SupportedCountry is created in case it does not exist. Normally all
     * SupportedCountry objects are created during DB setup.
     */
    public static SupportedCountry findOrCreate(DataService mgr,
            String countryCode) throws NonUniqueBusinessKeyException {
        SupportedCountry result = (SupportedCountry) mgr
                .find(new SupportedCountry(countryCode));
        if (result == null) {
            result = persistCountry(mgr, countryCode);
        }
        return result;
    }

    /**
     * Creates one supported country. One country is the minimum setup to pass
     * validation tests in the business logic.
     * 
     * @param mgr
     * @throws NonUniqueBusinessKeyException
     */
    public static void createOneSupportedCountry(DataService mgr)
            throws NonUniqueBusinessKeyException {
        findOrCreate(mgr, Locale.GERMANY.getCountry());
    }

    /**
     * Creates a few supported countries for testing. This method is faster than
     * creating all countries.
     * 
     * @param mgr
     * @throws NonUniqueBusinessKeyException
     */
    public static void createSomeSupportedCountries(DataService mgr)
            throws NonUniqueBusinessKeyException {
        findOrCreate(mgr, Locale.GERMANY.getCountry());
        findOrCreate(mgr, Locale.JAPAN.getCountry());
        findOrCreate(mgr, Locale.UK.getCountry());
    }

    /**
     * Creates all SupportedCountry objects. Normally all SupportedCountry
     * objects are created during DB setup. Creating all countries is slow and
     * should be done only if needed.
     */
    public static void createAllSupportedCountries(DataService mgr)
            throws NonUniqueBusinessKeyException {
        for (String countryCode : Locale.getISOCountries()) {
            findOrCreate(mgr, countryCode);
        }
    }

    /*
     * Create a supported country for the given country code.
     */
    private static SupportedCountry persistCountry(DataService mgr,
            String countryCode) throws NonUniqueBusinessKeyException {
        SupportedCountry country = new SupportedCountry(countryCode);
        mgr.persist(country);
        return country;
    }

    /**
     * Setup one country for testing. The platform operator is created with one
     * supported country. Only minimum data is created for performance reasons.
     * 
     * @param mgr
     * @throws Exception
     */
    public static void setupOneCountry(DataService mgr) throws Exception {
        createOneSupportedCountry(mgr);
        createPlatformOperator(mgr);
    }

    /**
     * Setup a few countries for testing. The platform operator is created. Only
     * a few countries are created for performance reasons.
     * 
     * @param mgr
     * @throws Exception
     */
    public static void setupSomeCountries(DataService mgr) throws Exception {
        createSomeSupportedCountries(mgr);
        createPlatformOperator(mgr);
    }

    /**
     * Setup all countries for testing. The platform operator is created with
     * all supported countries. Creating all countries is slow and should be
     * done only when needed.
     * 
     * @param mgr
     * @throws Exception
     */
    public static void setupAllCountries(DataService mgr) throws Exception {
        createAllSupportedCountries(mgr);
        createPlatformOperator(mgr);
    }

    /*
     * Create the platform operator. The platform operator defines the supported
     * countries for all supplieres.
     */
    private static Organization createPlatformOperator(DataService mgr)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Organization operator = Organizations.findOrganization(mgr,
                "PLATFORM_OPERATOR");
        if (operator == null) {
            operator = Organizations.createOrganization(mgr);
            operator.setOrganizationId("PLATFORM_OPERATOR");
        }
        Organizations.supportAllCountries(mgr, operator);
        return operator;
    }

    /**
     * Searches the SupportedCountry for the given country code.
     * 
     * @param mgr
     * @param countryCode
     * @return
     */
    public static SupportedCountry find(DataService mgr, String countryCode) {
        SupportedCountry result = (SupportedCountry) mgr
                .find(new SupportedCountry(countryCode));
        return result;
    }

}
