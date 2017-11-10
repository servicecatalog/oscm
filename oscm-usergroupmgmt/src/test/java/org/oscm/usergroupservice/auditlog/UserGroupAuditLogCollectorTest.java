/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 7, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.usergroupservice.auditlog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.oscm.auditlog.util.AuditLogData;
import org.oscm.auditlog.util.AuditLogParameter;
import org.oscm.auditlog.util.BESAuditLogEntry;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.UserGroup;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author zhaoh.fnst
 * 
 */
public class UserGroupAuditLogCollectorTest {

    private Marketplace marketplace;

    private final static String ORGANIZATIONID = "organizationid";
    private final static String VENDORID_1 = "VENDORID_1";
    private final static String VENDORID_2 = "VENDORID_2";
    private final static String USERID_1 = "userid_1";
    private final static String USERID_2 = "userid_2";
    private final static String GROUPNAME_1 = "groupname_1";
    private final static String GROUPNAME_2 = "groupname_2";
    private final static String PRODUCTID_1 = "productid_1";
    private final static String PRODUCTID_2 = "productid_2";
    private final static String MARKETPLACENAME = "marketplacename";
    private final static String PRODUCTNAME = "productname";
    private final static String PARAMETERNAME = "parametername";
    private final static long PRODUCTKEY_1 = 25566L;
    private final static long PRODUCTKEY_2 = 28966L;
    private final static String MARKETPLACEID = "marketplaceid";

    private static DataService dsMock;
    private static UserGroupAuditLogCollector logCollector = new UserGroupAuditLogCollector();

    @Before
    public void setup() throws Exception {
        dsMock = mock(DataService.class);
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATIONID);
        PlatformUser user = new PlatformUser();
        user.setUserId(USERID_1);
        user.setLocale("en");
        user.setOrganization(org);
        when(dsMock.getCurrentUser()).thenReturn(user);
        marketplace = new Marketplace();
        marketplace.setKey(1000l);
        doReturn(marketplace).when(dsMock).getReferenceByBusinessKey(
                any(Marketplace.class));
    }

    @Test
    public void accessToServices_enable() throws ObjectNotFoundException {
        // given
        givenPartameterName();

        // when
        accessToServices(UserGroupAuditLogOperation.ENABLE_ACCESS_TO_SERVICES,
                givenGroup(), givenProducts());

        // then
        accessToServices_VerifyLogEntries(UserGroupAuditLogOperation.ENABLE_ACCESS_TO_SERVICES);
    }

    @Test
    public void accessToServices_disable() throws ObjectNotFoundException {
        // given
        givenPartameterName();

        // when
        accessToServices(UserGroupAuditLogOperation.DISABLE_ACCESS_TO_SERVICES,
                givenGroup(), givenProducts());

        // then
        accessToServices_VerifyLogEntries(UserGroupAuditLogOperation.DISABLE_ACCESS_TO_SERVICES);
    }

    @Test
    public void assignUserToGroups() {
        // when
        assignUserToGroups(givenGroups(), givenUser());

        // then
        assignUserToGroups_VerifyLogEntries();
    }

    @Test
    public void assignUsersToGroup() {
        // when
        assignUsersToGroup(givenGroup(), givenUsers());

        // then
        assignUsersToGroup_VerifyLogEntries();
    }

    @Test
    public void removeUserFromGroups() {
        // when
        removeUserFromGroups(givenGroups(), givenUser());

        // then
        removeUserFromGroups_VerifyLogEntries();
    }

    @Test
    public void removeUsersFromGroup() {
        // when
        removeUsersFromGroup(givenGroup(), givenUsers());

        // then
        removeUsersFromGroup_VerifyLogEntries();
    }

    @Test
    public void getMarketplaceName() throws Exception {
        // given
        givenMarketplaceName();

        // when
        String result = logCollector
                .getMarketplaceName(dsMock, "marketplaceId");

        // then
        assertEquals(MARKETPLACENAME, result);
    }

    @Test
    public void getProductName() throws Exception {
        // given
        List<Product> products = new ArrayList<Product>();
        Product prod = new Product();
        prod.setKey(1000L);
        products.add(prod);
        givenProductName();

        // when
        Map<Long, String> result = logCollector
                .getProductName(dsMock, products);

        // then
        assertEquals(1, result.size());
        assertEquals(PRODUCTNAME, result.get(Long.valueOf(1000L)));
    }

    private UserGroupAuditLogCollector accessToServices(
            UserGroupAuditLogOperation operation, UserGroup group,
            List<Product> products) throws ObjectNotFoundException {
        AuditLogData.clear();
        logCollector.accessToServices(dsMock, operation, group, products,
                MARKETPLACEID);
        return logCollector;
    }

    private UserGroupAuditLogCollector assignUserToGroups(
            List<UserGroup> groups, PlatformUser user) {
        AuditLogData.clear();
        logCollector.assignUserToGroups(dsMock, groups, user);
        return logCollector;
    }

    private UserGroupAuditLogCollector assignUsersToGroup(UserGroup group,
            List<PlatformUser> users) {
        AuditLogData.clear();
        logCollector.assignUsersToGroup(dsMock, group, users);
        return logCollector;
    }

    private UserGroupAuditLogCollector removeUserFromGroups(
            List<UserGroup> groups, PlatformUser user) {
        AuditLogData.clear();
        logCollector.removeUserFromGroups(dsMock, groups, user);
        return logCollector;
    }

    private UserGroupAuditLogCollector removeUsersFromGroup(UserGroup group,
            List<PlatformUser> users) {
        AuditLogData.clear();
        logCollector.removeUsersFromGroup(dsMock, group, users);
        return logCollector;
    }

    private void accessToServices_VerifyLogEntries(
            UserGroupAuditLogOperation operation) {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(2, logEntries.size());
        BESAuditLogEntry logEntry1 = (BESAuditLogEntry) AuditLogData.get().get(
                0);
        assertEquals(operation.name().toString(), logEntry1.getOperationName());
        Map<AuditLogParameter, String> logParams1 = logEntry1
                .getLogParameters();
        assertEquals(GROUPNAME_1, logParams1.get(AuditLogParameter.GROUP));
        assertEquals(MARKETPLACEID,
                logParams1.get(AuditLogParameter.MARKETPLACE_ID));
        assertEquals(PARAMETERNAME,
                logParams1.get(AuditLogParameter.MARKETPLACE_NAME));
        assertEquals(PRODUCTID_1, logParams1.get(AuditLogParameter.SERVICE_ID));
        assertEquals(PARAMETERNAME,
                logParams1.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(VENDORID_1, logParams1.get(AuditLogParameter.SELLER_ID));
    }

    private void assignUserToGroups_VerifyLogEntries() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(2, logEntries.size());
        BESAuditLogEntry logEntry1 = (BESAuditLogEntry) AuditLogData.get().get(
                0);
        Map<AuditLogParameter, String> logParams1 = logEntry1
                .getLogParameters();
        assertEquals(GROUPNAME_1, logParams1.get(AuditLogParameter.GROUP));
        assertEquals(USERID_1, logParams1.get(AuditLogParameter.USER));

        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(1);
        Map<AuditLogParameter, String> logParams2 = logEntry.getLogParameters();
        assertEquals(GROUPNAME_2, logParams2.get(AuditLogParameter.GROUP));
        assertEquals(USERID_1, logParams2.get(AuditLogParameter.USER));
    }

    private void assignUsersToGroup_VerifyLogEntries() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry1 = (BESAuditLogEntry) AuditLogData.get().get(
                0);
        Map<AuditLogParameter, String> logParams1 = logEntry1
                .getLogParameters();
        assertEquals(GROUPNAME_1, logParams1.get(AuditLogParameter.GROUP));
        assertEquals(USERID_1 + "," + USERID_2,
                logParams1.get(AuditLogParameter.USER));
    }

    private void removeUserFromGroups_VerifyLogEntries() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(2, logEntries.size());
        BESAuditLogEntry logEntry1 = (BESAuditLogEntry) AuditLogData.get().get(
                0);
        Map<AuditLogParameter, String> logParams1 = logEntry1
                .getLogParameters();
        assertEquals(GROUPNAME_1, logParams1.get(AuditLogParameter.GROUP));
        assertEquals(USERID_1, logParams1.get(AuditLogParameter.USER));

        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(1);
        Map<AuditLogParameter, String> logParams2 = logEntry.getLogParameters();
        assertEquals(GROUPNAME_2, logParams2.get(AuditLogParameter.GROUP));
        assertEquals(USERID_1, logParams2.get(AuditLogParameter.USER));
    }

    private void removeUsersFromGroup_VerifyLogEntries() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry1 = (BESAuditLogEntry) AuditLogData.get().get(
                0);
        Map<AuditLogParameter, String> logParams1 = logEntry1
                .getLogParameters();
        assertEquals(GROUPNAME_1, logParams1.get(AuditLogParameter.GROUP));
        assertEquals(USERID_1 + "," + USERID_2,
                logParams1.get(AuditLogParameter.USER));
    }

    private List<UserGroup> givenGroups() {
        List<UserGroup> groups = new ArrayList<UserGroup>();
        UserGroup group1 = new UserGroup();
        group1.setName(GROUPNAME_1);
        UserGroup group2 = new UserGroup();
        group2.setName(GROUPNAME_2);
        groups.add(group1);
        groups.add(group2);

        return groups;
    }

    private PlatformUser givenUser() {
        PlatformUser user = new PlatformUser();
        user.setUserId(USERID_1);

        return user;
    }

    private List<PlatformUser> givenUsers() {
        List<PlatformUser> users = new ArrayList<PlatformUser>();
        PlatformUser user1 = new PlatformUser();
        user1.setUserId(USERID_1);
        PlatformUser user2 = new PlatformUser();
        user2.setUserId(USERID_2);
        users.add(user1);
        users.add(user2);

        return users;
    }

    private UserGroup givenGroup() {
        UserGroup group = new UserGroup();
        group.setName(GROUPNAME_1);

        return group;
    }

    private List<Product> givenProducts() {
        List<Product> products = new ArrayList<Product>();

        Product product1 = new Product();
        product1.setKey(PRODUCTKEY_1);
        product1.setProductId(PRODUCTID_1);
        Organization org1 = new Organization();
        org1.setOrganizationId(VENDORID_1);
        product1.setVendor(org1);
        Product product2 = new Product();
        product2.setProductId(PRODUCTID_2);
        product2.setKey(PRODUCTKEY_2);
        Organization org2 = new Organization();
        org2.setOrganizationId(VENDORID_2);
        product2.setVendor(org2);
        products.add(product1);
        products.add(product2);
        return products;
    }

    private void givenMarketplaceName() {
        LocalizedResource resource = new LocalizedResource();
        resource.setValue(MARKETPLACENAME);
        doReturn(resource).when(dsMock).find(any(LocalizedResource.class));
    }

    private void givenProductName() {
        LocalizedResource resource = new LocalizedResource();
        resource.setValue(PRODUCTNAME);
        doReturn(resource).when(dsMock).find(any(LocalizedResource.class));
    }

    private void givenPartameterName() {
        LocalizedResource resource = new LocalizedResource();
        resource.setValue(PARAMETERNAME);
        doReturn(resource).when(dsMock).find(any(LocalizedResource.class));
    }
}
