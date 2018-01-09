/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
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
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * @author zhaohang
 */

public class ServiceAuditLogCollector_updateServiceParameter_IT extends
        EJBTestBase {

    private final static String PARAMETERNAME = "parametername";
    private final static boolean USEOPTION1 = false;
    private final static boolean USEOPTION2 = true;
    private final static String PARAMETERVALUE = "parametervalue";
    private final static String ORGANIZATIONID = "organizationid";
    private final static String USERID = "userid";
    private final static String USEROPTIONFALSE = "OFF";
    private final static String USEROPTIONTRUE = "ON";
    private final static long SERVICEID = 56556;
    private final static long SERVICEID_0 = 0;
    private final static String SERVICENAME = "servicename";

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
    public void insertServiceParameter() {

        // given
        Product product = givenProduct();

        // when
        updateServiceParameter(PARAMETERNAME, PARAMETERVALUE, USEOPTION1,
                product);

        // then
        verifyLogEntries(true, USEROPTIONFALSE);
    }

    @Test
    public void updateServiceParameter() {

        // given
        Product product = givenProduct();

        // when
        updateServiceParameter(PARAMETERNAME, PARAMETERVALUE, USEOPTION2,
                product);

        // then
        verifyLogEntries(true, USEROPTIONTRUE);
    }

    @Test
    public void deleteServiceParameter() {

        // given
        Product product = givenProduct();

        // when
        updateServiceParameter(PARAMETERNAME, "", USEOPTION1, product);

        // then
        verifyLogEntries(false, USEROPTIONFALSE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateServiceParameter_ZEROKey() {

        // given
        Product product = givenProduct_ZEROKey();

        // when
        updateServiceParameter(PARAMETERNAME, PARAMETERVALUE, USEOPTION1,
                product);
    }

    private ServiceAuditLogCollector updateServiceParameter(
            String parameterName, String parameterValue,
            boolean isConfigurable, Product product) {
        AuditLogData.clear();
        logCollector.updateServiceParameters(dsMock, product, parameterName,
                parameterValue, isConfigurable);
        return logCollector;
    }

    private void verifyLogEntries(boolean isUpdateAction, String userOption) {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(SERVICENAME, logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(PARAMETERNAME,
                logParams.get(AuditLogParameter.PARAMETER_NAME));
        if (isUpdateAction) {
            assertEquals(userOption,
                    logParams.get(AuditLogParameter.USEROPTION));
            assertEquals(PARAMETERVALUE,
                    logParams.get(AuditLogParameter.PARAMETER_VALUE));
        } else {
            assertEquals("", logParams.get(AuditLogParameter.PARAMETER_VALUE));
            assertEquals(userOption,
                    logParams.get(AuditLogParameter.USEROPTION));
        }
    }

    private Product givenProduct() {
        Product product = new Product();
        product.setKey(SERVICEID);
        product.setProductId(SERVICENAME);
        return product;
    }

    private Product givenProduct_ZEROKey() {
        Product product = new Product();
        product.setKey(SERVICEID_0);
        product.setProductId(SERVICENAME);
        return product;
    }
}
