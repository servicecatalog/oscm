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
 * @author zhaohang
 */

public class ServiceAuditLogCollector_updateService_IT extends EJBTestBase {
    private final static boolean DESCRIPTIONCHANGED = true;
    private final static boolean SHORTDESCRIPTIONCHANGED = true;
    private final static boolean DESCRIPTIONNOTCHANGED = false;
    private final static boolean SHORTDESCRIPTIONNOTCHANGED = false;
    private static final boolean CUSTOMTABCHANGED = true;
    private static final boolean CUSTOMTABNOTCHANGED = false;
    private final static String ORGANIZATIONID = "organizationid";
    private final static String LOCALE = "en";
    private final static String USERID = "userid";
    private final static long SERVICEID = 76234;
    private final static String SERVICENAME = "servicename";

    private static DataService dsMock;
    private static ServiceAuditLogCollector logCollector = new ServiceAuditLogCollector();
    private static LocalizerServiceLocal localizerMock;
    private final static String LOCALIZED_RESOURCE = "TEST";

    private final static String HAVEDESCRIPTION = "YES";
    private final static String HAVENODESCRIPTION = "NO";
    private final static String HAVESHORTDESCRIPTION = "YES";
    private final static String HAVENOSHORTDESCRIPTION = "NO";
    private final static String HAVECUSTOMTABNAME = "YES";
    private final static String HAVENOCUSTOMTABNAME = "NO";
    private final static boolean AUTOASSIGNUSERENABLED = true;
    private final static boolean AUTOASSIGNUSERDISABLED = false;

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
    public void updateService_DescriptionAndShortDescription() {

        // given
        Product product = givenProduct();

        // when
        updateService(product, DESCRIPTIONCHANGED, SHORTDESCRIPTIONCHANGED, CUSTOMTABNOTCHANGED);

        // then
        verifyLogEntries(HAVEDESCRIPTION, HAVESHORTDESCRIPTION, HAVENOCUSTOMTABNAME,
                AUTOASSIGNUSERDISABLED);
    }

    @Test
    public void updateService_DescriptionAndNOShortDescription() {

        // given
        Product product = givenProduct();

        // when
        updateService(product, DESCRIPTIONCHANGED, SHORTDESCRIPTIONNOTCHANGED, CUSTOMTABNOTCHANGED);

        // then
        verifyLogEntries(HAVEDESCRIPTION, HAVENOSHORTDESCRIPTION, HAVENOCUSTOMTABNAME,
                AUTOASSIGNUSERDISABLED);
    }

    @Test
    public void updateService_NODescriptionAndShortDescription() {

        // given
        Product product = givenProduct();

        // when
        updateService(product, DESCRIPTIONNOTCHANGED, SHORTDESCRIPTIONCHANGED, CUSTOMTABNOTCHANGED);

        // then
        verifyLogEntries(HAVENODESCRIPTION, HAVESHORTDESCRIPTION, HAVENOCUSTOMTABNAME,
                AUTOASSIGNUSERDISABLED);
    }

    @Test
    public void updateService_NODescriptionAndNOShortDescription() {

        // given
        Product product = givenProduct();

        // when
        updateService(product, DESCRIPTIONNOTCHANGED,
                SHORTDESCRIPTIONNOTCHANGED, CUSTOMTABNOTCHANGED);

        // then
        verifyLogEntries(HAVENODESCRIPTION, HAVENOSHORTDESCRIPTION, HAVENOCUSTOMTABNAME,
                AUTOASSIGNUSERDISABLED);
    }

    @Test
    public void updateService_AutoAssignUserEnabled() {

        // given
        Product product = givenProduct();
        product.setAutoAssignUserEnabled(Boolean.TRUE);
        // when
        updateService(product, DESCRIPTIONNOTCHANGED,
                SHORTDESCRIPTIONNOTCHANGED, CUSTOMTABNOTCHANGED);

        // then
        verifyLogEntries(HAVENODESCRIPTION, HAVENOSHORTDESCRIPTION, HAVENOCUSTOMTABNAME,
                AUTOASSIGNUSERENABLED);
    }

    @Test
    public void updateService_CustomTabNameOnly() {
        // given
        Product product = givenProduct();
        product.setAutoAssignUserEnabled(Boolean.TRUE);
        // when
        updateService(product, DESCRIPTIONNOTCHANGED,
                SHORTDESCRIPTIONNOTCHANGED, CUSTOMTABCHANGED);

        // then
        verifyLogEntries(HAVENODESCRIPTION, HAVENOSHORTDESCRIPTION, HAVECUSTOMTABNAME,
                AUTOASSIGNUSERENABLED);
    }

    @Test
    public void updateService_CustomTabAndDesc() {
        // given
        Product product = givenProduct();
        product.setAutoAssignUserEnabled(Boolean.TRUE);
        // when
        updateService(product, DESCRIPTIONCHANGED,
                SHORTDESCRIPTIONNOTCHANGED, CUSTOMTABCHANGED);

        // then
        verifyLogEntries(HAVEDESCRIPTION, HAVENOSHORTDESCRIPTION, HAVECUSTOMTABNAME,
                AUTOASSIGNUSERENABLED);
    }

    @Test
    public void updateService_CustomTabAndShortDesc() {
        // given
        Product product = givenProduct();
        product.setAutoAssignUserEnabled(Boolean.TRUE);
        // when
        updateService(product, DESCRIPTIONNOTCHANGED,
                SHORTDESCRIPTIONCHANGED, CUSTOMTABCHANGED);

        // then
        verifyLogEntries(HAVENODESCRIPTION, HAVESHORTDESCRIPTION, HAVECUSTOMTABNAME,
                AUTOASSIGNUSERENABLED);
    }

    @Test
    public void updateService_CustomTabAndShortDescAndDesc() {
        // given
        Product product = givenProduct();
        product.setAutoAssignUserEnabled(Boolean.TRUE);
        // when
        updateService(product, DESCRIPTIONCHANGED,
                SHORTDESCRIPTIONCHANGED, CUSTOMTABCHANGED);

        // then
        verifyLogEntries(HAVEDESCRIPTION, HAVESHORTDESCRIPTION, HAVECUSTOMTABNAME,
                AUTOASSIGNUSERENABLED);
    }

    private ServiceAuditLogCollector updateService(Product product,
            boolean isDescriptionChanged, boolean isShortDescriptionChanged, boolean isCustomTabChanged) {
        AuditLogData.clear();
        logCollector.updateService(dsMock, product, isShortDescriptionChanged,
                isDescriptionChanged, isCustomTabChanged, LOCALE);
        return logCollector;
    }

    private void verifyLogEntries(String isHaveDescription,
            String isHaveShortDescription, String isHaveCustomTabName, boolean autoAssignUserEnabled) {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(SERVICENAME, logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(isHaveShortDescription,
                logParams.get(AuditLogParameter.SHORT_DESCRIPTION));
        assertEquals(isHaveDescription,
                logParams.get(AuditLogParameter.DESCRIPTION));
        assertEquals(isHaveCustomTabName,
                logParams.get(AuditLogParameter.CUSTOM_TAB_NAME));
        assertEquals(LOCALE, logParams.get(AuditLogParameter.LOCALE));
        assertEquals((autoAssignUserEnabled ? "YES" : "NO"),
                logParams.get(AuditLogParameter.AUTO_ASSIGN_USER));
    }

    private Product givenProduct() {
        Product product = new Product();
        product.setKey(SERVICEID);
        product.setProductId(SERVICENAME);
        product.setAutoAssignUserEnabled(Boolean.FALSE);
        return product;
    }

}
