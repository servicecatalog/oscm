/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Aleh Khomich                 
 *                                                                              
 *  Creation Date: 19.05.2010                                                     
 *                                                                              
 *  Completion Time: 19.05.2010
 *                                              
 *******************************************************************************/
package org.oscm.domobjects;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.domobjects.enums.OrganizationReferenceType;

/**
 * Test class for Discount domain object.
 * 
 * @author Aleh Khomich.
 * 
 */
public class DiscountIT extends DomainObjectTestBase {

    /**
     * Test discount creation.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateThrowOrganizationUpdate() throws Exception {

        final BigDecimal value = new BigDecimal("5.01");
        final BigDecimal value2 = new BigDecimal("10.00");
        final Long startTime = Long.valueOf(40L);
        final Long endTime = Long.valueOf(60L);

        final Organization organization = runTX(new Callable<Organization>() {
            public Organization call() throws Exception {
                Organization organization = createCustomerAndSupplier();
                return organization;
            }
        });

        final Discount discount = runTX(new Callable<Discount>() {
            public Discount call() throws Exception {
                final long key = createDiscount(organization, value, startTime,
                        endTime);
                mgr.flush();
                Discount discount = mgr.find(Discount.class, key);
                return discount;
            }
        });

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Discount discountBefore = mgr.find(Discount.class,
                        discount.getKey());
                discountBefore.setValue(value2);
                mgr.flush();

                Discount discountAfter = mgr.find(Discount.class,
                        discount.getKey());

                Assert.assertEquals(discount.getKey(), discountAfter.getKey());
                Assert.assertEquals(value2, discountAfter.getValue());
                Assert.assertEquals(startTime, discountAfter.getStartTime());
                Assert.assertEquals(endTime, discountAfter.getEndTime());

                return null;
            }
        });
    }

    /**
     * Test discount creation.
     * 
     * @throws Exception
     */
    @Test
    public void testCreation() throws Exception {

        final BigDecimal value = new BigDecimal("5.01");

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                final long key = doCreate(value);

                Discount discount = mgr.find(Discount.class, key);

                Assert.assertEquals(key, discount.getKey());
                Assert.assertEquals(value, discount.getValue());

                return null;
            }
        });
    }

    /**
     * Test discount modify.
     * 
     * @throws Exception
     */
    @Test
    public void testModify() throws Exception {
        final BigDecimal value = new BigDecimal("5.01");

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                final long key = doCreate(value);

                Discount discount = mgr.find(Discount.class, key);

                final BigDecimal newValue = new BigDecimal("99.01");
                discount.setValue(newValue);

                discount = mgr.find(Discount.class, key);

                Assert.assertEquals(key, discount.getKey());
                Assert.assertEquals(newValue, discount.getValue());

                return null;
            }
        });
    }

    /**
     * Test discount delete.
     * 
     * @throws Exception
     */
    @Test
    public void testDelete() throws Exception {
        final BigDecimal value = new BigDecimal("5.01");

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                final long key = doCreate(value);

                Discount discount = mgr.find(Discount.class, key);

                mgr.remove(discount);

                discount = mgr.find(Discount.class, key);

                Assert.assertEquals(null, discount);

                return null;
            }
        });
    }

    /**
     * Test cascade delete. Not business case. organization is never deleted.
     * 
     * @throws Exception
     */
    @Test
    public void testCascadeDeleteOrganization() throws Exception {

        final BigDecimal value = new BigDecimal("5.01");

        // create discount and test cascade delete form organization table
        final Organization organization = runTX(new Callable<Organization>() {
            public Organization call() throws Exception {
                Organization organization = createCustomerAndSupplier();
                return organization;
            }
        });

        final Long discountKey = runTX(new Callable<Long>() {
            public Long call() throws Exception {
                long key = createDiscount(organization, value, null, null);
                return Long.valueOf(key);
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Organization orgTmp = mgr.find(Organization.class,
                        organization.getKey());
                mgr.remove(orgTmp);
                return null;
            }
        });

        // in another transaction looking for discount
        // had to be deleted on previous step
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Discount discount = mgr.find(Discount.class, discountKey);
                Assert.assertEquals(null, discount);

                return null;
            }
        });
    }

    /**
     * Create discount.
     * 
     * @param value
     * @return
     * @throws Exception
     */
    private long doCreate(BigDecimal value) throws Exception {

        Organization organization = createCustomerAndSupplier();

        final long key = createDiscount(organization, value, null, null);

        return key;
    }

    /**
     * Helper method for discount creating.
     * 
     * @param value
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    private long createDiscount(Organization organization, BigDecimal value,
            Long startTime, Long endTime) throws Exception {
        Discount discount = new Discount();

        organization = mgr.getReference(Organization.class,
                organization.getKey());
        discount.setValue(value);
        discount.setStartTime(startTime);
        discount.setEndTime(endTime);
        discount.setOrganizationReference(organization.getSources().get(0));
        mgr.persist(discount);
        final long key = discount.getKey();

        return key;
    }

    /**
     * Helper method for organization creating.
     * 
     * @return
     * @throws Exception
     */
    private Organization createCustomerAndSupplier() throws Exception {
        Organization organization = new Organization();
        organization.setOrganizationId("testOrg");
        organization.setRegistrationDate(123L);
        organization.setCutOffDay(1);
        mgr.persist(organization);

        Organization supplier = new Organization();
        supplier.setOrganizationId("supplier");
        supplier.setRegistrationDate(123L);
        supplier.setCutOffDay(1);
        mgr.persist(supplier);

        OrganizationReference ref = new OrganizationReference(supplier,
                organization, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        mgr.persist(ref);
        mgr.flush();
        mgr.refresh(supplier);
        mgr.refresh(organization);
        return organization;
    }

}
