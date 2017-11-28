/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.auditlog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.oscm.internal.vo.VOCategory;

/**
 * @author Min CHEN
 * 
 */
public class MarketplaceAuditLogCollectorIT extends EJBTestBase {

    private final static long SERVICE_ID = 10000;
    private final static String SERVICE_NAME = "example";
    private final static boolean SERVICE_PUBLIC = true;
    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static String MARKETPLACE_ID = "marketplaceId";
    private final static String MARKETPLACE_NAME = "marketplaceName";
    private final static String CATAGORY_ID1 = "1";
    private final static String CATAGORY_ID2 = "2";
    private final static String CATEGORIES_ID = "1,2";

    private static DataService dsMock;

    private static LocalizerServiceLocal localizerMock;
    private final static String LOCALIZED_RESOURCE = "TEST";
    private static MarketplaceAuditLogCollector logCollector = new MarketplaceAuditLogCollector();

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
    public void setServiceAsPublic() {
        AuditLogData.clear();
        logCollector
                .setServiceAsPublic(dsMock, createService(), SERVICE_PUBLIC);
        // then
        verifyLogEntriesForSetPublicService();
    }

    private void verifyLogEntriesForSetPublicService() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(String.valueOf(SERVICE_NAME),
                logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(Boolean.valueOf(SERVICE_PUBLIC).toString(),
                logParams.get(AuditLogParameter.SERVICE_PUBLIC));
    }

    @Test
    public void assignToMarketPlace() {
        AuditLogData.clear();
        logCollector.assignToMarketPlace(dsMock, createService(),
                MARKETPLACE_ID, MARKETPLACE_NAME);
        // then
        verifyLogEntriesForAssignToMarketplace();
    }

    private void verifyLogEntriesForAssignToMarketplace() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(String.valueOf(SERVICE_NAME),
                logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(MARKETPLACE_ID,
                logParams.get(AuditLogParameter.MARKETPLACE_ID));
        assertEquals(MARKETPLACE_NAME,
                logParams.get(AuditLogParameter.MARKETPLACE_NAME));
    }

    @Test
    public void assignCategories() {
        AuditLogData.clear();
        logCollector
                .assignCategories(dsMock, createService(), createCatagory());
        // then
        verifyLogEntriesForAssignCategories();
    }

    private void verifyLogEntriesForAssignCategories() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(String.valueOf(SERVICE_NAME),
                logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(CATEGORIES_ID,
                logParams.get(AuditLogParameter.CATEGORIES_ID));
    }

    private Product createService() {
        Product prod = new Product();
        prod.setKey(SERVICE_ID);
        prod.setProductId(SERVICE_NAME);
        prod.setTemplate(prod);
        return prod;
    }

    private List<VOCategory> createCatagory() {
        List<VOCategory> categories = new ArrayList<VOCategory>();
        VOCategory category = new VOCategory();
        category.setCategoryId(CATAGORY_ID1);
        categories.add(category);
        VOCategory category2 = new VOCategory();
        category2.setCategoryId(CATAGORY_ID2);
        categories.add(category2);
        return categories;
    }

}
