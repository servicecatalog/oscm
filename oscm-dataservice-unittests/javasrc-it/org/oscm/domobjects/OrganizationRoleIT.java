/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 20.08.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBTransactionRolledbackException;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Test cases for the role handling of a organization. It covers testing the
 * objects
 * 
 * <ul>
 * <li>{@link OrganizationRole}</li>
 * <li>OrganizationToRole</li>
 * </ul>
 * 
 * @author Mike J&auml;ger
 * 
 */
public class OrganizationRoleIT extends DomainObjectTestBase {

    private List<OrganizationRole> roles = new ArrayList<OrganizationRole>();
    private List<OrganizationToRole> orgToRoles = new ArrayList<OrganizationToRole>();

    private List<Organization> orgs = new ArrayList<Organization>();

    /**
     * Creates an organization role entry in the database, retrieves it later on
     * and compares the entries.
     * 
     * <p>
     * Expected result is that the retrieved element matches the one stored
     * initially.
     * </p>
     */
    @Test
    public void testRoleCreation() throws Exception {
        final OrganizationRole role = new OrganizationRole();
        role.setRoleName(OrganizationRoleType.CUSTOMER);
        persistDO(role);
        roles.add(role);

        final OrganizationRole storedRole = (OrganizationRole) getStoredDOviaBK(role);

        Assert.assertNotNull("Object not found in database", storedRole);
        Assert.assertEquals("Technical key does not match", role.getKey(),
                storedRole.getKey());
        Assert.assertEquals("Wrong role name stored", role.getRoleName(),
                storedRole.getRoleName());
    }

    /**
     * Tries to create two organization role entries with the same business key.
     * This violates the unique constraint and must fail.
     */
    @Test
    public void testRoleCreationViolateUniqueConstraint() throws Exception {
        final OrganizationRole role1 = new OrganizationRole();
        role1.setRoleName(OrganizationRoleType.CUSTOMER);

        final OrganizationRole role2 = new OrganizationRole();
        role2.setRoleName(OrganizationRoleType.CUSTOMER);

        persistDO(role1);

        try {
            persistDO(role2);
            Assert.fail("Storing the value must fail, as second entry has business key identical to first object");
        } catch (NonUniqueBusinessKeyException e) {
            // verify that role1 still exists but role2 does not
            OrganizationRole storedRole1 = (OrganizationRole) getStoredDOviaBK(role1);
            Assert.assertNotNull("Object not stored", storedRole1);

            OrganizationRole storedRole2 = (OrganizationRole) getStoredDOviaBK(role2);
            Assert.assertNotNull("Txn not rolled back", storedRole2);
        }
        roles.add(role1);
    }

    /**
     * Removes an organization role entry and finally tries to retrieve it
     * again. The final call must return null to verify the object deletion.
     */
    @Test
    public void testRoleDeletion() throws Exception {
        final OrganizationRole role = new OrganizationRole();
        role.setRoleName(OrganizationRoleType.CUSTOMER);
        persistDO(role);

        removeBasedOnBK(role);

        OrganizationRole storedRole = (OrganizationRole) getStoredDOviaBK(role);

        Assert.assertNull("Object has not been removed!", storedRole);
    }

    /**
     * Creates an organization role, an organization and a reference between
     * both of them (granting the organization the role). Creation of history
     * entry will also be checked.
     */
    @Test
    public void testCreateOrganizationToRole() throws Exception {
        // first create a role entry
        final OrganizationRole role = new OrganizationRole();
        role.setRoleName(OrganizationRoleType.CUSTOMER);
        persistDO(role);
        roles.add(role);

        doCreateCustomerOrgs();

        final OrganizationToRole orgToRole = new OrganizationToRole();
        orgToRole.setOrganization(orgs.get(0));
        orgToRole.setOrganizationRole(role);
        persistDO(orgToRole);

        orgToRoles.add(orgToRole);
        // now retrieve the reference and test it
        final OrganizationToRole storedOrgToRole = runTX(new Callable<OrganizationToRole>() {
            public OrganizationToRole call() throws Exception {
                OrganizationToRole storedOrgToRole = doGetStoredOrgToRoleviaTK(orgToRole);
                load(storedOrgToRole.getOrganizationRole());
                return storedOrgToRole;
            }
        });

        Assert.assertNotNull("Object was not stored", storedOrgToRole);
        Assert.assertEquals("Wrong role name stored", role.getRoleName(),
                storedOrgToRole.getOrganizationRole().getRoleName());
        Assert.assertEquals("Wrong object reference for role", role.getKey(),
                storedOrgToRole.getOrganizationRole().getKey());

        // due to lazy loading, the last assertion must be done in new
        // transaction
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Assert.assertEquals("Wrong object reference for organization",
                        orgs.get(0).getKey(),
                        doGetStoredOrgToRoleviaTK(storedOrgToRole)
                                .getOrganization().getKey());
                return null;
            }
        });

        // check the organization
        Organization storedOrg = (Organization) getStoredDOviaBK(orgs.get(0));
        Assert.assertEquals("Wrong number of organization roles stored", 1,
                storedOrg.getGrantedRoles().size());
        Assert.assertTrue("Wrong role assigned to organization",
                storedOrg.hasRole(role.getRoleName()));

        // check the history
        List<DomainHistoryObject<?>> hist = runTX(new Callable<List<DomainHistoryObject<?>>>() {
            public List<DomainHistoryObject<?>> call() throws Exception {
                return mgr.findHistory(storedOrgToRole);
            }
        });
        Assert.assertEquals("Wrong number of history entries created", 1,
                hist.size());
        Assert.assertEquals("Wrong mod type for history entry",
                ModificationType.ADD, hist.get(0).getModtype());
    }

    /**
     * Tries to create a organization/role reference that already exists in the
     * same constellation. This should end up in an exception.
     */
    @Test
    public void testCreateOrganizationToRoleDuplicate() throws Exception {
        final OrganizationRole role = new OrganizationRole();
        role.setRoleName(OrganizationRoleType.SUPPLIER);
        persistDO(role);
        roles.add(role);

        doCreateCustomerOrgs();

        final OrganizationToRole orgToRole1 = new OrganizationToRole();
        orgToRole1.setOrganization(orgs.get(0));
        orgToRole1.setOrganizationRole(role);
        persistDO(orgToRole1);
        orgToRoles.add(orgToRole1);

        final OrganizationToRole orgToRole2 = new OrganizationToRole();
        orgToRole2.setOrganization(orgs.get(0));
        orgToRole2.setOrganizationRole(role);
        try {
            persistDO(orgToRole2);
            Assert.fail("Object must not be stored due to unique constraint violation");
        } catch (EJBTransactionRolledbackException e) {
            // now ensure that the second store operation did not pass
            OrganizationToRole storedOrgToRole = runTX(new Callable<OrganizationToRole>() {
                public OrganizationToRole call() throws Exception {
                    return doGetStoredOrgToRoleviaTK(orgToRole2);
                }
            });
            Assert.assertNull("Transaction has not been rolled back",
                    storedOrgToRole);

            // also ensure that no history has been created
            List<?> hist = getHistoryForDO(orgToRole2);
            Assert.assertEquals(
                    "Transaction rollback did not remove history entries", 0,
                    hist.size());
        }
        orgToRoles.add(orgToRole2);
    }

    /**
     * Assignes two roles to an organization and verifies the stored data.
     */
    @Test
    public void testCreateOrganizationToRoleOneOrgMultipleRoles()
            throws Exception {
        // create roles
        OrganizationRole suppRole = new OrganizationRole();
        suppRole.setRoleName(OrganizationRoleType.SUPPLIER);
        persistDO(suppRole);
        roles.add(suppRole);

        doCreateCustomerOrgs();

        OrganizationRole custRole = new OrganizationRole();
        custRole.setRoleName(OrganizationRoleType.CUSTOMER);
        persistDO(custRole);
        roles.add(custRole);

        // assign roles to organization
        OrganizationToRole orgToRole1 = new OrganizationToRole();
        orgToRole1.setOrganization(orgs.get(2));
        orgToRole1.setOrganizationRole(suppRole);
        persistDO(orgToRole1);
        orgToRoles.add(orgToRole1);

        OrganizationToRole orgToRole2 = new OrganizationToRole();
        orgToRole2.setOrganization(orgs.get(2));
        orgToRole2.setOrganizationRole(custRole);
        persistDO(orgToRole2);
        orgToRoles.add(orgToRole2);

        // now check the organization
        Organization storedOrg = (Organization) getStoredDOviaBK(orgs.get(2));
        Assert.assertEquals("Wrong number of assigned roles", 2, storedOrg
                .getGrantedRoles().size());
        Assert.assertTrue("Wrong role assigned to organization",
                storedOrg.hasRole(suppRole.getRoleName()));
        Assert.assertTrue("Wrong role assigned to organization",
                storedOrg.hasRole(custRole.getRoleName()));
        Assert.assertTrue("Wrong role assigned to organization",
                storedOrg.hasRole(OrganizationRoleType.CUSTOMER));
        Assert.assertTrue("Wrong role assigned to organization",
                storedOrg.hasRole(OrganizationRoleType.SUPPLIER));
        Assert.assertFalse("Wrong role assigned to organization",
                storedOrg.hasRole(OrganizationRoleType.PLATFORM_OPERATOR));
        Assert.assertFalse("Wrong role assigned to organization",
                storedOrg.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER));
    }

    /**
     * Removes an organization to role object and checks if the entry is deleted
     * as well as if its history entries are correct.
     */
    @Test
    public void testRemoveOrganizationRole() throws Exception {
        final OrganizationRole role = new OrganizationRole();
        role.setRoleName(OrganizationRoleType.PLATFORM_OPERATOR);
        persistDO(role);
        roles.add(role);

        doCreateCustomerOrgs();

        final OrganizationToRole orgToRole1 = new OrganizationToRole();
        orgToRole1.setOrganization(orgs.get(0));
        orgToRole1.setOrganizationRole(role);
        persistDO(orgToRole1);
        orgToRoles.add(orgToRole1);

        // now remove the entry and check history

        removeOrganizationToRoleBasedOnTK(orgToRole1);
        orgToRoles.remove(orgToRole1);

        List<?> hist = getHistoryForDO(orgToRole1);
        Assert.assertEquals("Wrong number of history entries created", 2,
                hist.size());
        Assert.assertEquals("Wrong mod type for first entry",
                ModificationType.ADD,
                ((DomainHistoryObject<?>) hist.get(0)).getModtype());
        Assert.assertEquals("Wrong mod type for second entry",
                ModificationType.DELETE,
                ((DomainHistoryObject<?>) hist.get(1)).getModtype());

        // also check the formerly connected organisation
        Organization storedOrg = (Organization) getStoredDOviaBK(orgs.get(0));
        Assert.assertEquals("Wrong number of assigned roles", 0, storedOrg
                .getGrantedRoles().size());
    }

    /**
     * Retrieves a stored domain object based on its business key. Does not work
     * for domain objects without business key.
     */
    private DomainObject<?> getStoredDOviaBK(final DomainObject<?> role)
            throws Exception {
        return runTX(new Callable<DomainObject<?>>() {
            public DomainObject<?> call() throws Exception {
                DomainObject<?> entity = mgr.find(role);
                if (entity instanceof Organization) {
                    for (OrganizationToRole orgToRole : ((Organization) entity)
                            .getGrantedRoles()) {
                        load(orgToRole.getOrganizationRole());
                    }
                }
                return entity;
            }
        });
    }

    private OrganizationToRole doGetStoredOrgToRoleviaTK(
            OrganizationToRole orgToRole) {
        return mgr.find(OrganizationToRole.class, orgToRole.getKey());
    }

    /**
     * Removes a domain object based on its business key.
     */
    private void removeBasedOnBK(final DomainObject<?> obj) throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                DomainObject<?> objToRemove = mgr.find(obj);
                mgr.remove(objToRemove);
                return null;
            }
        });
    }

    /**
     * Removes an OrganizationToRole object based on its technical key.
     */
    private void removeOrganizationToRoleBasedOnTK(final DomainObject<?> obj)
            throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                DomainObject<?> objToRemove = mgr.find(
                        OrganizationToRole.class, obj.getKey());
                mgr.remove(objToRemove);
                return null;
            }
        });
    }

    private void doCreateCustomerOrgs() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {

                // Enter new Organizations
                Organization cust;
                for (int i = 1; i < 10; i++) {
                    cust = new Organization();
                    cust.setOrganizationId("Organization" + i);
                    cust.setName("The " + i + ". organization");
                    cust.setAddress("my address is a very long string, which is stored in the database \n with line delimiters\n.");
                    cust.setEmail("organization@organization" + i + ".com");
                    cust.setPhone("012345/678" + i + i + i);
                    cust.setCutOffDay(1);
                    mgr.persist(cust);
                    orgs.add(cust);
                }
                return null;
            }
        });
    }

    private List<DomainHistoryObject<?>> getHistoryForDO(
            final OrganizationToRole orgToRole2) throws Exception {
        return runTX(new Callable<List<DomainHistoryObject<?>>>() {
            public List<DomainHistoryObject<?>> call() throws Exception {
                return mgr.findHistory(orgToRole2);
            }
        });
    }

    private void persistDO(final DomainObject<?> obj) throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                mgr.persist(obj);
                return null;
            }
        });
    }

}
