/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-5-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.auditlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.auditlog.util.AuditLogData;
import org.oscm.auditlog.util.AuditLogParameter;
import org.oscm.auditlog.util.BESAuditLogEntry;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOPriceModelLocalization;

/**
 * @author Mao
 * 
 */
public class SubscriptionAuditLogCollector_LocalizePriceModelTest {

    private final static long SUBSCRIPTION_KEY = 1;
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static String ORGANIZATION_NAME = "organization_name";
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";
    private final static String LOCALE_EN = "en";
    private final static String LOCALE_DE = "de";
    private final static String LOCALE_JA = "ja";
    private static int DESCRIPTION_LOCALIZED = 1;
    private static int LICENSE_LOCALIZED = 0;

    private static DataService dsMock;
    VOPriceModelLocalization originalLocalization = new VOPriceModelLocalization();
    VOPriceModelLocalization newLocalization = new VOPriceModelLocalization();

    private static LocalizerServiceLocal localizerMock;
    private final static String LOCALIZED_RESOURCE = "TEST";
    private static SubscriptionAuditLogCollector logCollector = new SubscriptionAuditLogCollector();

    @BeforeClass
    public static void setup() {
        dsMock = mock(DataService.class);
        when(dsMock.getCurrentUser()).thenReturn(givenUser(LOCALE_EN));

        localizerMock = mock(LocalizerServiceLocal.class);
        when(
                localizerMock.getLocalizedTextFromDatabase(Mockito.anyString(),
                        Mockito.anyLong(),
                        Mockito.any(LocalizedObjectTypes.class))).thenReturn(
                                LOCALIZED_RESOURCE);
        logCollector.localizer = localizerMock;
    }

    private static PlatformUser givenUser(String locale) {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        org.setName(ORGANIZATION_NAME);
        PlatformUser user = new PlatformUser();
        user.setUserId(USER_ID);
        user.setOrganization(org);
        user.setLocale(locale);
        return user;
    }

    @Test
    public void localizePriceModel_OK() {
        // given
        Subscription sub = givenSubscription();
        originalLocalization = givenStoredPriceModelLocalization();
        newLocalization = givenNewPriceModelLocalization();
        // when
        localizeService(sub, originalLocalization, newLocalization);

        // then
        verifyLogEntries();
    }

    @Test
    public void localizePriceModel_SaveWithNoOperation() {
        // given
        Subscription sub = givenSubscription();
        originalLocalization = givenPriceModelLocalization(null);
        newLocalization = givenPriceModelLocalization("");
        // when
        localizeService(sub, originalLocalization, newLocalization);

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);
    }

    @Test
    public void localizePriceModel_NullorEmpty() {
        // given
        Subscription sub = givenSubscription();
        // when
        localizeService(sub, new VOPriceModelLocalization(),
                new VOPriceModelLocalization());

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);

    }

    @Test
    public void prepareLocalizationMap_OK() {
        // given
        newLocalization = givenNewPriceModelLocalization();

        // when
        SubscriptionAuditLogCollector logCollector = new SubscriptionAuditLogCollector();
        Map<String, LocalizedAuditLogEntryParameters> mapToBeUpdate = logCollector
                .prepareLocalizationMap(newLocalization);

        verifyLocalizationMap(mapToBeUpdate);

    }

    @Test
    public void collectLocalizationStates_OK() {
        // given
        SubscriptionAuditLogCollector logCollector = new SubscriptionAuditLogCollector();
        Map<String, LocalizedAuditLogEntryParameters> mapToBeUpdate = logCollector
                .prepareLocalizationMap(givenNewPriceModelLocalization());
        Map<String, LocalizedAuditLogEntryParameters> mapStored = logCollector
                .prepareLocalizationMap(givenStoredPriceModelLocalization());

        // when
        Map<String, BitSet> localeMap = logCollector.collectLocalizationStates(
                mapToBeUpdate, mapStored);

        verifyLocalizationStates(localeMap);

    }

    private void verifyLocalizationStates(Map<String, BitSet> localeMap) {
        assertEquals(2, localeMap.entrySet().size());
        assertEquals(
                Boolean.TRUE,
                new Boolean(localeMap.get(LOCALE_EN).get(DESCRIPTION_LOCALIZED)));
        assertEquals(Boolean.TRUE,
                new Boolean(localeMap.get(LOCALE_EN).get(LICENSE_LOCALIZED)));
        assertEquals(
                Boolean.TRUE,
                new Boolean(localeMap.get(LOCALE_JA).get(DESCRIPTION_LOCALIZED)));
        assertEquals(
                Boolean.TRUE,
                new Boolean(localeMap.get(LOCALE_JA).get(DESCRIPTION_LOCALIZED)));

    }

    private void verifyLocalizationMap(
            Map<String, LocalizedAuditLogEntryParameters> localizationMap) {
        assertEquals(2, localizationMap.entrySet().size());
        assertEquals(Boolean.TRUE, new Boolean(localizationMap.get(LOCALE_EN)
                .getDescription().equals(LOCALE_EN)));
        assertEquals(Boolean.TRUE, new Boolean(localizationMap.get(LOCALE_EN)
                .getLicense().equals(LOCALE_EN)));
        assertEquals(Boolean.TRUE, new Boolean(localizationMap.get(LOCALE_JA)
                .getDescription().isEmpty()));
        assertEquals(Boolean.TRUE, new Boolean(localizationMap.get(LOCALE_JA)
                .getLicense().isEmpty()));
    }

    private void verifyLogEntries() {

        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(2, logEntries.size());
        BESAuditLogEntry logEntry1 = (BESAuditLogEntry) AuditLogData.get().get(
                0);
        BESAuditLogEntry logEntry2 = (BESAuditLogEntry) AuditLogData.get().get(
                1);
        Map<AuditLogParameter, String> logParams1 = logEntry1
                .getLogParameters();
        Map<AuditLogParameter, String> logParams2 = logEntry2
                .getLogParameters();
        assertEquals(PRODUCT_ID, logParams1.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams1.get(AuditLogParameter.SERVICE_NAME));

        assertEquals(LOCALE_EN, logParams1.get(AuditLogParameter.LOCALE));
        assertEquals("YES", logParams1.get(AuditLogParameter.DESCRIPTION));
        assertEquals("YES", logParams1.get(AuditLogParameter.LICENSE));

        assertEquals(LOCALE_JA, logParams2.get(AuditLogParameter.LOCALE));
        assertEquals("YES", logParams2.get(AuditLogParameter.DESCRIPTION));
        assertEquals("YES", logParams2.get(AuditLogParameter.LICENSE));
    }

    private VOPriceModelLocalization givenNewPriceModelLocalization() {
        VOPriceModelLocalization localization = new VOPriceModelLocalization();
        List<VOLocalizedText> localizedText = new ArrayList<VOLocalizedText>();
        localizedText.add(new VOLocalizedText(LOCALE_EN, LOCALE_EN));
        localizedText.add(new VOLocalizedText(LOCALE_JA, ""));
        localization.setDescriptions(localizedText);
        localization.setLicenses(localizedText);
        return localization;
    }

    private VOPriceModelLocalization givenStoredPriceModelLocalization() {
        VOPriceModelLocalization localization = new VOPriceModelLocalization();
        List<VOLocalizedText> localizedText = new ArrayList<VOLocalizedText>();
        localizedText
                .add(new VOLocalizedText(LOCALE_EN, LOCALE_EN + LOCALE_EN));
        localizedText.add(new VOLocalizedText(LOCALE_JA, LOCALE_JA));
        localization.setDescriptions(localizedText);
        localization.setLicenses(localizedText);
        return localization;
    }

    private VOPriceModelLocalization givenPriceModelLocalization(String content) {
        VOPriceModelLocalization localization = new VOPriceModelLocalization();
        List<VOLocalizedText> localizedText = new ArrayList<VOLocalizedText>();
        localizedText.add(new VOLocalizedText(LOCALE_DE, content));
        localization.setDescriptions(localizedText);
        localization.setLicenses(localizedText);
        return localization;
    }

    private Product givenService() {
        Product prod = new Product();
        prod.setKey(PRODUCT_KEY);
        prod.setProductId(PRODUCT_ID);
        prod.setTemplate(prod);
        return prod;
    }

    private Subscription givenSubscription() {
        Subscription sub = new Subscription();
        sub.setKey(SUBSCRIPTION_KEY);
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setProduct(givenService());
        sub.setOrganization(givenCustomer());
        return sub;
    }

    private Organization givenCustomer() {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        org.setName(ORGANIZATION_NAME);
        return org;
    }

    private SubscriptionAuditLogCollector localizeService(
            Subscription subscription,
            VOPriceModelLocalization oldLocalization,
            VOPriceModelLocalization localization) {
        AuditLogData.clear();
        logCollector.localizePriceModel(dsMock, subscription, oldLocalization,
                localization);
        return logCollector;
    }

}
