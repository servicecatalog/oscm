/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-5-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

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
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

/**
 * @author zhao
 */
public class ServiceAuditLogCollector_activeOrDeactiveService_IT extends
        EJBTestBase {

    private final static String ORGANIZATIONID = "organizationid";
    private final static String USERID = "userid";
    private final static long SERVICEID = 76234;
    private final static String SERVICENAME = "servicename";
    private final static String MARKETPLACEID = "marketplaceid";
    private final static String MARKETPLACENAME = "marketplacename";
    private final static boolean ACTIVE = true;
    private final static boolean DEACTIVE = false;
    private final static boolean INCATALOG = true;
    private final static boolean NOTINCATALOG = false;
    private final static String ACTIVEVALUE = "ON";
    private final static String DEACTIVEVALUE = "OFF";
    private final static String INCATALOGVALUE = "ON";
    private final static String NOTINCATALOGVALUE = "OFF";

    private static DataService dsMock;
    private static ServiceAuditLogCollector logCollector = new ServiceAuditLogCollector();
    private static LocalizerServiceLocal localizerMock;
    private final static String LOCALIZED_RESOURCE = "TEST";

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
        org.setOrganizationId(ORGANIZATIONID);
        PlatformUser user = new PlatformUser();
        user.setUserId(USERID);
        user.setOrganization(org);
        return user;
    }

    @Test
    public void activeOrDeActiveService_ActiveAndIncatalog() {

        // given
        Product product = givenProduct();

        // when
        activeOrDeactiveService(product, ACTIVE, INCATALOG);

        // then
        verifyLogEntries(ACTIVEVALUE, INCATALOGVALUE);
    }

    @Test
    public void activeOrDeActiveService_DeactiveAndIncatalog() {

        // given
        Product product = givenProduct();

        // when
        activeOrDeactiveService(product, ACTIVE, NOTINCATALOG);

        // then
        verifyLogEntries(ACTIVEVALUE, NOTINCATALOGVALUE);
    }

    @Test
    public void activeOrDeActiveService_ActiveAndNotincatalog() {

        // given
        Product product = givenProduct();

        // when
        activeOrDeactiveService(product, DEACTIVE, INCATALOG);

        // then
        verifyLogEntries(DEACTIVEVALUE, INCATALOGVALUE);
    }

    @Test
    public void activeOrDeActiveService_DeactiveAndNotincatalog() {

        // given
        Product product = givenProduct();

        // when
        activeOrDeactiveService(product, DEACTIVE, NOTINCATALOG);

        // then
        verifyLogEntries(DEACTIVEVALUE, NOTINCATALOGVALUE);
    }

    private ServiceAuditLogCollector activeOrDeactiveService(Product product,
            boolean isActive, boolean Incatalog) {
        AuditLogData.clear();
        logCollector.activeOrDeactiveService(dsMock, product, MARKETPLACEID,
                MARKETPLACENAME, isActive, Incatalog);
        return logCollector;
    }

    private void verifyLogEntries(String isActiveValue, String incatalogValue) {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(SERVICENAME, logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(MARKETPLACEID,
                logParams.get(AuditLogParameter.MARKETPLACE_ID));
        assertEquals(MARKETPLACENAME,
                logParams.get(AuditLogParameter.MARKETPLACE_NAME));
        assertEquals(isActiveValue, logParams.get(AuditLogParameter.ACTIVATION));
        assertEquals(incatalogValue, logParams.get(AuditLogParameter.INCATALOG));
    }

    private Product givenProduct() {
        Product product = new Product();
        product.setKey(SERVICEID);
        product.setProductId(SERVICENAME);
        return product;
    }

}
