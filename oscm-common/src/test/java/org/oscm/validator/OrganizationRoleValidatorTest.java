/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 6, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.validator;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.IncompatibleRolesException;
import org.oscm.internal.types.exception.OperationNotPermittedException;

public class OrganizationRoleValidatorTest {

    @Test
    public void containsMultipleSellerRoles_onlyBroker()
            throws IncompatibleRolesException {
        OrganizationRoleValidator.containsMultipleSellerRoles(
                new HashSet<OrganizationRoleType>(Arrays
                        .asList(OrganizationRoleType.BROKER)), null);
    }

    @Test
    public void containsMultipleSellerRoles_onlyReseller()
            throws IncompatibleRolesException {
        OrganizationRoleValidator.containsMultipleSellerRoles(
                new HashSet<OrganizationRoleType>(Arrays
                        .asList(OrganizationRoleType.RESELLER)), null);
    }

    @Test
    public void containsMultipleSellerRoles_onlySupplier()
            throws IncompatibleRolesException {
        OrganizationRoleValidator.containsMultipleSellerRoles(
                new HashSet<OrganizationRoleType>(Arrays
                        .asList(OrganizationRoleType.SUPPLIER)), null);
    }

    @Test
    public void containsMultipleSellerRoles_onlyTechnologyProvider()
            throws IncompatibleRolesException {
        OrganizationRoleValidator.containsMultipleSellerRoles(
                new HashSet<OrganizationRoleType>(Arrays
                        .asList(OrganizationRoleType.TECHNOLOGY_PROVIDER)),
                null);
    }

    @Test(expected = IncompatibleRolesException.class)
    public void containsMultipleSellerRoles_brokerAndReseller()
            throws IncompatibleRolesException {
        OrganizationRoleValidator
                .containsMultipleSellerRoles(
                        new HashSet<OrganizationRoleType>(Arrays.asList(
                                OrganizationRoleType.RESELLER,
                                OrganizationRoleType.BROKER)),
                        LogMessageIdentifier.WARN_REGISTER_ORGANIZATION_FAILED_INCOMPATIBLE_ROLES_GRANTED);
        fail();
    }

    @Test(expected = IncompatibleRolesException.class)
    public void containsMultipleSellerRoles_brokerAndSupplier()
            throws IncompatibleRolesException {
        OrganizationRoleValidator
                .containsMultipleSellerRoles(
                        new HashSet<OrganizationRoleType>(Arrays.asList(
                                OrganizationRoleType.SUPPLIER,
                                OrganizationRoleType.BROKER)),
                        LogMessageIdentifier.WARN_REGISTER_ORGANIZATION_FAILED_INCOMPATIBLE_ROLES_GRANTED);
        fail();
    }

    @Test(expected = IncompatibleRolesException.class)
    public void containsMultipleSellerRoles_supplierAndReseller()
            throws IncompatibleRolesException {
        OrganizationRoleValidator
                .containsMultipleSellerRoles(
                        new HashSet<OrganizationRoleType>(Arrays.asList(
                                OrganizationRoleType.RESELLER,
                                OrganizationRoleType.BROKER)),
                        LogMessageIdentifier.WARN_REGISTER_ORGANIZATION_FAILED_INCOMPATIBLE_ROLES_GRANTED);
        fail();
    }

    @Test(expected = IncompatibleRolesException.class)
    public void containsMultipleSellerRoles_brokerAndResellerAndSupplier()
            throws IncompatibleRolesException {
        OrganizationRoleValidator
                .containsMultipleSellerRoles(
                        new HashSet<OrganizationRoleType>(Arrays.asList(
                                OrganizationRoleType.RESELLER,
                                OrganizationRoleType.BROKER,
                                OrganizationRoleType.SUPPLIER)),
                        LogMessageIdentifier.WARN_REGISTER_ORGANIZATION_FAILED_INCOMPATIBLE_ROLES_GRANTED);
        fail();
    }

    @Test(expected = IncompatibleRolesException.class)
    public void containsMultipleSellerRoles_brokerAndTechnologyProvider()
            throws IncompatibleRolesException {
        OrganizationRoleValidator
                .containsMultipleSellerRoles(
                        new HashSet<OrganizationRoleType>(Arrays.asList(
                                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                                OrganizationRoleType.BROKER)),
                        LogMessageIdentifier.WARN_REGISTER_ORGANIZATION_FAILED_INCOMPATIBLE_ROLES_GRANTED);
        fail();
    }

    @Test(expected = IncompatibleRolesException.class)
    public void containsMultipleSellerRoles_resellerAndTechnologyProvider()
            throws IncompatibleRolesException {
        OrganizationRoleValidator
                .containsMultipleSellerRoles(
                        new HashSet<OrganizationRoleType>(Arrays.asList(
                                OrganizationRoleType.RESELLER,
                                OrganizationRoleType.TECHNOLOGY_PROVIDER)),
                        LogMessageIdentifier.WARN_REGISTER_ORGANIZATION_FAILED_INCOMPATIBLE_ROLES_GRANTED);
        fail();
    }

    @Test
    public void containsMultipleSellerRoles_supplierAndTechnologyProvider()
            throws IncompatibleRolesException {
        OrganizationRoleValidator
                .containsMultipleSellerRoles(
                        new HashSet<OrganizationRoleType>(Arrays.asList(
                                OrganizationRoleType.SUPPLIER,
                                OrganizationRoleType.TECHNOLOGY_PROVIDER)),
                        LogMessageIdentifier.WARN_REGISTER_ORGANIZATION_FAILED_INCOMPATIBLE_ROLES_GRANTED);
    }

    @Test
    public void containsSupplier_Supplier()
            throws OperationNotPermittedException {
        OrganizationRoleValidator.containsSupplier(
                Collections.singleton(OrganizationRoleType.SUPPLIER), "ABC");
    }

    @Test(expected = OperationNotPermittedException.class)
    public void containsSupplier_Broker() throws OperationNotPermittedException {
        OrganizationRoleValidator.containsSupplier(
                Collections.singleton(OrganizationRoleType.BROKER), "ABC");
    }

    @Test(expected = OperationNotPermittedException.class)
    public void containsSupplier_Reseller()
            throws OperationNotPermittedException {
        OrganizationRoleValidator.containsSupplier(
                Collections.singleton(OrganizationRoleType.RESELLER), "ABC");
    }

}
