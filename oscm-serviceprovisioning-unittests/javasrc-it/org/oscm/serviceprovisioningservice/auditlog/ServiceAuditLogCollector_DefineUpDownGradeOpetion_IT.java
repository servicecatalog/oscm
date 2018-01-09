/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2013-5-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.auditlog.AuditLogData;
import org.oscm.auditlog.AuditLogParameter;
import org.oscm.auditlog.BESAuditLogEntry;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

/**
 * @author Mao
 * 
 */
public class ServiceAuditLogCollector_DefineUpDownGradeOpetion_IT extends
        EJBTestBase {

    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static String ORGANIZATION_NAME = "organization_name";
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";
    private final static long TARGET_PRODUCT_KEY_NEW_1 = 100;
    private final static String TARGET_PRODUCT_ID_NEW_1 = "product_id_1";
    private final static long TARGET_PRODUCT_KEY_NEW_2 = 200;
    private final static String TARGET_PRODUCT_ID_NEW_2 = "product_id_2";
    private final static long TARGET_PRODUCT_KEY_NEW_3 = 300;
    private final static String TARGET_PRODUCT_ID_NEW_3 = "product_id_3";
    private final static long TARGET_PRODUCT_KEY_REMOVE_4 = 400;
    private final static String TARGET_PRODUCT_ID_REMOVE_4 = "product_id_4";
    private final static long TARGET_PRODUCT_KEY_REMOVE_5 = 500;
    private final static String TARGET_PRODUCT_ID_REMOVE_5 = "product_id_5";
    private final static String UPDOWNGRADE_ON = "ON";
    private final static String UPDOWNGRADE_OFF = "OFF";

    private static DataService dsMock;
    private static ServiceAuditLogCollector logCollector = new ServiceAuditLogCollector();
    private static LocalizerServiceLocal localizerMock;
    private final static String LOCALIZED_RESOURCE = "TEST";

    private List<Product> newReference = new ArrayList<Product>();
    private List<Product> removeReference = new ArrayList<Product>();

    @Override
    protected void setup(TestContainer container) {
        container.enableInterfaceMocking(true);
        dsMock = mock(DataService.class);
        when(dsMock.getCurrentUser()).thenReturn(givenUser());

        localizerMock = mock(LocalizerServiceLocal.class);
        when(
                localizerMock.getLocalizedTextFromDatabase(Mockito.anyString(),
                        Mockito.anyLong(),
                        Mockito.any(LocalizedObjectTypes.class))).thenReturn(
                LOCALIZED_RESOURCE);
        logCollector.localizer = localizerMock;
    }

    private PlatformUser givenUser() {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        org.setName(ORGANIZATION_NAME);
        PlatformUser user = new PlatformUser();
        user.setUserId(USER_ID);
        user.setOrganization(org);
        return user;
    }

    @Test
    public void defineUpDownGradeOption_OK() {
        // given
        Product referenctProduct = givenReferenceService();
        newReference.clear();
        removeReference.clear();
        newReference = givenNewTargerProductReference();
        removeReference = givenOldTargerProductReference();
        AuditLogData.clear();
        // when
        for (Product product : newReference) {
            defineUpDownGradeService(referenctProduct, product, UPDOWNGRADE_ON);
        }
        for (Product product : removeReference) {
            defineUpDownGradeService(referenctProduct, product, UPDOWNGRADE_OFF);
        }

        // then
        verifyLogEntries();

    }

    @Test
    public void defineUpDownGradeOption_Empty() {
        // given
        Product referenctProduct = givenReferenceService();
        newReference.clear();
        removeReference.clear();
        AuditLogData.clear();
        // when
        for (Product product : newReference) {
            defineUpDownGradeService(referenctProduct, product, UPDOWNGRADE_ON);
        }

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);

    }

    private void verifyLogEntries() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(5, logEntries.size());
        BESAuditLogEntry logEntry1 = (BESAuditLogEntry) AuditLogData.get().get(
                0);
        BESAuditLogEntry logEntry2 = (BESAuditLogEntry) AuditLogData.get().get(
                1);
        BESAuditLogEntry logEntry3 = (BESAuditLogEntry) AuditLogData.get().get(
                2);
        BESAuditLogEntry logEntry4 = (BESAuditLogEntry) AuditLogData.get().get(
                3);
        BESAuditLogEntry logEntry5 = (BESAuditLogEntry) AuditLogData.get().get(
                4);

        assertEquals(
                TARGET_PRODUCT_ID_NEW_1,
                logEntry1.getLogParameters().get(
                        AuditLogParameter.TARGET_SERVICE_ID));
        assertEquals(
                LOCALIZED_RESOURCE,
                logEntry2.getLogParameters().get(
                        AuditLogParameter.TARGET_SERVICE_NAME));
        assertEquals(
                LOCALIZED_RESOURCE,
                logEntry3.getLogParameters().get(
                        AuditLogParameter.TARGET_SERVICE_NAME));
        assertEquals(
                LOCALIZED_RESOURCE,
                logEntry4.getLogParameters().get(
                        AuditLogParameter.TARGET_SERVICE_NAME));
        assertEquals(TARGET_PRODUCT_ID_REMOVE_5, logEntry5.getLogParameters()
                .get(AuditLogParameter.TARGET_SERVICE_ID));

        assertEquals(UPDOWNGRADE_ON,
                logEntry1.getLogParameters().get(AuditLogParameter.UPDOWNGRADE));
        assertEquals(UPDOWNGRADE_ON,
                logEntry2.getLogParameters().get(AuditLogParameter.UPDOWNGRADE));
        assertEquals(UPDOWNGRADE_ON,
                logEntry3.getLogParameters().get(AuditLogParameter.UPDOWNGRADE));
        assertEquals(UPDOWNGRADE_OFF,
                logEntry4.getLogParameters().get(AuditLogParameter.UPDOWNGRADE));
        assertEquals(UPDOWNGRADE_OFF,
                logEntry5.getLogParameters().get(AuditLogParameter.UPDOWNGRADE));

    }

    private Product givenReferenceService() {
        Product prod = new Product();
        prod.setKey(PRODUCT_KEY);
        prod.setProductId(PRODUCT_ID);
        prod.setTemplate(prod);
        return prod;
    }

    private List<Product> givenNewTargerProductReference() {
        List<Product> products = new ArrayList<Product>();
        products.add(givenTargetProduct(TARGET_PRODUCT_KEY_NEW_1,
                TARGET_PRODUCT_ID_NEW_1));
        products.add(givenTargetProduct(TARGET_PRODUCT_KEY_NEW_2,
                TARGET_PRODUCT_ID_NEW_2));
        products.add(givenTargetProduct(TARGET_PRODUCT_KEY_NEW_3,
                TARGET_PRODUCT_ID_NEW_3));
        return products;
    }

    private List<Product> givenOldTargerProductReference() {
        List<Product> products = new ArrayList<Product>();
        products.add(givenTargetProduct(TARGET_PRODUCT_KEY_REMOVE_4,
                TARGET_PRODUCT_ID_REMOVE_4));
        products.add(givenTargetProduct(TARGET_PRODUCT_KEY_REMOVE_5,
                TARGET_PRODUCT_ID_REMOVE_5));
        return products;
    }

    private Product givenTargetProduct(long key, String Id) {
        Product prod = new Product();
        prod.setKey(key);
        prod.setProductId(Id);
        return prod;
    }

    private ServiceAuditLogCollector defineUpDownGradeService(Product product,
            Product targetProduct, String upDownGrade) {
        logCollector.defineUpDownGradeOptions(dsMock, product, targetProduct,
                upDownGrade);
        return logCollector;
    }

}
