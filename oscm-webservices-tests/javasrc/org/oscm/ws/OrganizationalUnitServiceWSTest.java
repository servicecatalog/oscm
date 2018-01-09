/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                  
 *  Creation Date: 22.07.15 12:00
 *
 *******************************************************************************/

package org.oscm.ws;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.oscm.converter.api.Converter;
import org.oscm.domobjects.UserGroup;
import org.oscm.intf.IdentityService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.OrganizationalUnitService;
import org.oscm.pagination.Pagination;
import org.oscm.types.enumtypes.Salutation;
import org.oscm.types.enumtypes.UnitRoleType;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.DeletionConstraintException;
import org.oscm.types.exceptions.MailOperationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationalUnit;
import org.oscm.vo.VOService;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;
import org.oscm.ws.unitrule.Order;
import org.oscm.ws.unitrule.OrderedRunner;

@RunWith(OrderedRunner.class)
public class OrganizationalUnitServiceWSTest {

    private static OrganizationalUnitService unitService;
    private static IdentityService identityService;
    private static VOOrganization supplier;
    private static VOFactory factory = new VOFactory();
    private static UserGroup userGroup;

    private static final long USER_KEY = 1000;
    private static final List<UserRoleType> ROLE_TYPES = Collections
            .singletonList(UserRoleType.ORGANIZATION_ADMIN);
    private static VOUserDetails USER;
    private static VOService VISIBLE_SERVICE;
    private static VOService ACCESSIBLE_SERVICE;
    private static VOService INVISIBLE_SERVICE;

    @BeforeClass

    public static void setUp() throws Exception {
        // clean the mails
        WebserviceTestBase.getMailReader().deleteMails();

        WebserviceTestSetup setup = new WebserviceTestSetup();

        // create supplier1
        supplier = setup.createSupplier("Supplier");

        // create mp
        setup.createTechnicalService();
        MarketplaceService mpSrvOperator = ServiceFactory.getDefault()
                .getMarketPlaceService(
                        WebserviceTestBase.getPlatformOperatorKey(),
                        WebserviceTestBase.getPlatformOperatorPassword());

        // Create a local marketplace
        VOMarketplace mpLocal = mpSrvOperator.createMarketplace(factory
                .createMarketplaceVO(supplier.getOrganizationId(), false,
                        "Local Marketplace"));

        // Retrieve org unit service
        unitService = ServiceFactory.getDefault()
                .getOrganizationalUnitService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        // Retrieve identity service
        identityService = ServiceFactory.getDefault()
                .getIdentityService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        // Create UserGroup
        final String unitName = randomString("TestUnit");
        final String unitDesc = randomString("TestDesc");
        final String unitRefId = randomString("refId");

        VOOrganizationalUnit unit = unitService.createUnit(unitName, unitDesc,
                unitRefId);

        userGroup = Converter.convert(unit, VOOrganizationalUnit.class,
                UserGroup.class);

        USER = identityService.createUser(createUser(), ROLE_TYPES,
                mpLocal.getMarketplaceId());


    }

    @Test
    @Order(order = 1)
    public void createUnit() throws OperationNotPermittedException,
            NonUniqueBusinessKeyException {
        // given
        final String unitName = randomString("TestUnit");
        final String unitDesc = randomString("TestDesc");
        final String unitRefId = randomString("refId");

        // when
        VOOrganizationalUnit unit = unitService.createUnit(unitName, unitDesc,
                unitRefId);

        // then
        Assert.assertNotNull(unit);
        Assert.assertEquals(unitName, unit.getName());
        Assert.assertEquals(unitDesc, unit.getDescription());
        Assert.assertEquals(unitRefId, unit.getReferenceId());
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    @Order(order = 2)
    public void createUnitExists() throws OperationNotPermittedException,
            NonUniqueBusinessKeyException {
        // given
        final String unitName = randomString("TestUnit");
        final String unitDesc = randomString("TestDesc");
        final String unitRefId = randomString("refId");

        // when
        unitService.createUnit(unitName, unitDesc, unitRefId);
        unitService.createUnit(unitName, unitDesc, unitRefId);

        // then exception
    }

    @Test
    @Order(order = 3)
    public void getOrganizationalUnits() throws OperationNotPermittedException,
            NonUniqueBusinessKeyException {
        // given
        final int expectedGroups = 4; // 3 created in tests before + default

        // when
        List<VOOrganizationalUnit> units = unitService
                .getOrganizationalUnits(null);

        // then
        Assert.assertNotNull(units);
        Assert.assertEquals(expectedGroups, units.size());
    }

    @Test
    @Order(order = 4)
    public void getOrganizationalUnitsWithPaging()
            throws OperationNotPermittedException {
        // given
        Pagination pagination = new Pagination(0, 2);

        // when
        List<VOOrganizationalUnit> units = unitService
                .getOrganizationalUnits(pagination);

        // then
        Assert.assertNotNull(units);
        Assert.assertEquals(2, units.size());
    }

    @Test
    @Order(order = 5)
    public void grantUserRoles() throws ObjectNotFoundException,
            NonUniqueBusinessKeyException, MailOperationException,
            OperationNotPermittedException {

        // given
        identityService.addRevokeUserUnitAssignment(userGroup.getName(),
                Collections.<VOUser>singletonList(USER),
                Collections.<VOUser>emptyList());

        // when and then
        VOOrganizationalUnit unit = getUnitWithName(userGroup.getName());

        Assert.assertNotNull(unit);
        unitService.revokeUserRoles(USER,
                Collections.singletonList(UnitRoleType.USER), unit);
        unitService.grantUserRoles(USER,
                Collections.singletonList(UnitRoleType.ADMINISTRATOR), unit);
    }

    @Test
    @Order(order = 6)
    public void revokeUserRoles() throws OperationNotPermittedException,
            ObjectNotFoundException, MailOperationException,
            NonUniqueBusinessKeyException {
        // given
        VOOrganizationalUnit unit = getUnitWithName(userGroup.getName());

        // when and then
        Assert.assertNotNull(unit);
        unitService.revokeUserRoles(USER,
                Collections.singletonList(UnitRoleType.ADMINISTRATOR), unit);
    }

    @Test
    @Order(order = 7)
    public void deleteUnit() throws NonUniqueBusinessKeyException,
            MailOperationException, ObjectNotFoundException,
            DeletionConstraintException, OperationNotPermittedException {
        // given
        final String unitName = randomString("TestUnit");
        final String unitDesc = randomString("TestDesc");
        final String unitRefId = randomString("refId");

        // when
        unitService.createUnit(unitName, unitDesc, unitRefId);
        unitService.deleteUnit(unitName);

        // then
        VOOrganizationalUnit unit = getUnitWithName(unitName);
        Assert.assertNull(unit);
    }

    @Test(expected = ObjectNotFoundException.class)
    @Order(order = 8)
    public void deleteUnitNotExist()
            throws OperationNotPermittedException, DeletionConstraintException,
            MailOperationException, ObjectNotFoundException {
        // given
        final String unitName = randomString("TestUnit");

        // when
        unitService.deleteUnit(unitName);

        // then exception
    }


    private static String randomString(String prefix) {
        return prefix + new BigInteger(130, new SecureRandom()).toString(32);
    }

    private static VOUserDetails createUser() throws Exception {
        VOUserDetails userDetails = factory.createUserVO(Long
                .toHexString(System.currentTimeMillis()));

        userDetails.setKey(USER_KEY);
        userDetails.setOrganizationId(supplier.getOrganizationId());
        userDetails.setUserId(randomString("TestUser"));
        userDetails.setAdditionalName("additionalName");
        userDetails.setAddress("address");
        userDetails.setFirstName("firstName");
        userDetails.setLastName("lastName");
        userDetails.setPhone("08154711");
        userDetails.setSalutation(Salutation.MR);

        return userDetails;
    }

    private VOOrganizationalUnit getUnitWithName(String unitName)
            throws OperationNotPermittedException {
        for (VOOrganizationalUnit unit : unitService
                .getOrganizationalUnits(null)) {
            if (unitName.equals(unit.getName())) {
                return unit;
            }
        }

        return null;
    }
}
