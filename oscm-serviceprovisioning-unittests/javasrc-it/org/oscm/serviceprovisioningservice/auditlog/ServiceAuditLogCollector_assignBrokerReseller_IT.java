/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 25.04.2013                                                      
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
import org.oscm.internal.types.enumtypes.OfferingType;

/**
 * @author Min CHEN
 * 
 */
public class ServiceAuditLogCollector_assignBrokerReseller_IT extends
        EJBTestBase {

    private final static long SERVICE_ID = 10000;
    private final static String SERVICE_NAME = "example";
    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static boolean IS_ASSIGN = true;

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
        org.setOrganizationId(ORGANIZATION_ID);
        PlatformUser user = new PlatformUser();
        user.setUserId(USER_ID);
        user.setOrganization(org);
        return user;
    }

    @Test
    public void assignResellerBroker() {
        AuditLogData.clear();

        logCollector.assignResellerBroker(dsMock, createService(),
                ORGANIZATION_ID, OfferingType.BROKER, IS_ASSIGN);

        logCollector.assignResellerBroker(dsMock, createService(),
                ORGANIZATION_ID, OfferingType.RESELLER, false);
        // then
        verifyLogEntriesForSetPublicService();
    }

    private void verifyLogEntriesForSetPublicService() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(2, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(logEntry.getOperationId(),
                ServiceAuditLogOperation.ASSIGN_SERVICE_BROKERS
                        .getOperationId());
        assertEquals(SERVICE_NAME, logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(ORGANIZATION_ID,
                logParams.get(AuditLogParameter.BROKER_ID));

        logEntry = (BESAuditLogEntry) AuditLogData.get().get(1);
        logParams = logEntry.getLogParameters();
        assertEquals(logEntry.getOperationId(),
                ServiceAuditLogOperation.DEASSIGN_SERVICE_RESELLER
                        .getOperationId());
        assertEquals(SERVICE_NAME, logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(ORGANIZATION_ID,
                logParams.get(AuditLogParameter.RESELLER_ID));
    }

    private Product createService() {
        Product prod = new Product();
        prod.setKey(SERVICE_ID);
        prod.setProductId(SERVICE_NAME);
        prod.setTemplate(prod);
        return prod;
    }
}
