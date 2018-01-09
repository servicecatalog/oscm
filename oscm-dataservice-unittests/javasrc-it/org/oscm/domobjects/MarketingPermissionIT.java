/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: kulle                                                  
 *                                                                              
 *  Creation Date: 01.12.2011                                                      
 *                                                                              
 *  Completion Time: 01.12.2011                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.TechnicalProducts;

/**
 * Tests for the domain object representing marketing permission
 * 
 * @author kulle
 */
public class MarketingPermissionIT extends DomainObjectTestBase {

    private List<DomainObjectWithEmptyDataContainer> domObjects = new ArrayList<DomainObjectWithEmptyDataContainer>();
    private Organization supplier;
    private Organization supplier2;
    private Organization supplier3;

    private Organization provider;
    private OrganizationReference reference;
    private OrganizationReference reference2;
    private OrganizationReference reference3;
    private TechnicalProduct technicalProduct;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();

        domObjects.clear();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                provider = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                mgr.persist(provider);

                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                mgr.persist(supplier);
                supplier2 = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                mgr.persist(supplier2);
                supplier3 = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                mgr.persist(supplier3);

                reference = Organizations
                        .createOrganizationReference(
                                provider,
                                supplier,
                                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER,
                                mgr);
                mgr.persist(reference);
                reference2 = Organizations
                        .createOrganizationReference(
                                provider,
                                supplier2,
                                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER,
                                mgr);
                mgr.persist(reference2);
                reference3 = Organizations
                        .createOrganizationReference(
                                provider,
                                supplier3,
                                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER,
                                mgr);
                mgr.persist(reference3);

                technicalProduct = TechnicalProducts.createTestData(mgr,
                        provider, 1).get(0);
                mgr.persist(technicalProduct);
                return null;
            }
        });
    }

    @Test
    public void testCreate() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketingPermission permission = new MarketingPermission();
                permission.setTechnicalProduct(technicalProduct);
                permission.setOrganizationReference(reference);
                mgr.persist(permission);
                domObjects.add((MarketingPermission) ReflectiveClone
                        .clone(permission));
                return null;
            }
        });

        // ASSERT
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketingPermission original = (MarketingPermission) domObjects
                        .get(0);
                MarketingPermission found = mgr.find(MarketingPermission.class,
                        original.getKey());
                assertNotNull(found);
                assertEquals(original.getTechnicalProduct().getKey(), found
                        .getTechnicalProduct().getKey());
                assertEquals(original.getOrganizationReference().getKey(),
                        found.getOrganizationReference().getKey());

                return null;
            }
        });
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testBusinessKeyException() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketingPermission permission = new MarketingPermission();
                permission.setTechnicalProduct(technicalProduct);
                permission.setOrganizationReference(reference);
                mgr.persist(permission);
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketingPermission permission = new MarketingPermission();
                permission.setTechnicalProduct(technicalProduct);
                permission.setOrganizationReference(reference);
                mgr.persist(permission);
                return null;
            }
        });
    }

    @Test
    public void testDelete() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketingPermission permission = new MarketingPermission();
                permission.setTechnicalProduct(technicalProduct);
                permission.setOrganizationReference(reference);
                mgr.persist(permission);
                domObjects.add((MarketingPermission) ReflectiveClone
                        .clone(permission));
                return null;
            }
        });

        // REMOVE
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketingPermission original = (MarketingPermission) domObjects
                        .get(0);
                MarketingPermission found = mgr.find(MarketingPermission.class,
                        original.getKey());
                mgr.remove(found);
                return null;
            }
        });

        // ASSERT
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketingPermission original = (MarketingPermission) domObjects
                        .get(0);
                MarketingPermission found = mgr.find(MarketingPermission.class,
                        original.getKey());
                assertNull(found);
                return null;
            }
        });

    }

    @Test
    public void testNamedQuery_FindForTechnicalService() throws Exception {
        // create marketing permission
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketingPermission permission = new MarketingPermission();
                permission.setTechnicalProduct(technicalProduct);
                permission.setOrganizationReference(reference);
                mgr.persist(permission);
                domObjects.add((MarketingPermission) ReflectiveClone
                        .clone(permission));
                return null;
            }
        });

        // run query
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Query query = mgr
                        .createNamedQuery("MarketingPermission.findForTechnicalService");
                query.setParameter("tp", technicalProduct);
                List<MarketingPermission> result = ParameterizedTypes.list(
                        query.getResultList(), MarketingPermission.class);

                // assert
                assertEquals(1, result.size());
                return null;
            }
        });
    }

    @Test
    public void testNamedQuery_FindForTechnicalServices() throws Exception {
        // create marketing permissions
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketingPermission permission = new MarketingPermission();
                permission.setTechnicalProduct(technicalProduct);
                permission.setOrganizationReference(reference);
                mgr.persist(permission);
                domObjects.add((MarketingPermission) ReflectiveClone
                        .clone(permission));

                MarketingPermission permission2 = new MarketingPermission();
                permission2.setTechnicalProduct(technicalProduct);
                permission2.setOrganizationReference(reference2);
                mgr.persist(permission2);
                domObjects.add((MarketingPermission) ReflectiveClone
                        .clone(permission2));

                MarketingPermission permission3 = new MarketingPermission();
                permission3.setTechnicalProduct(technicalProduct);
                permission3.setOrganizationReference(reference3);
                mgr.persist(permission3);
                domObjects.add((MarketingPermission) ReflectiveClone
                        .clone(permission3));

                return null;
            }
        });

        // run query
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Query query = mgr
                        .createNamedQuery("MarketingPermission.findForTechnicalService");
                query.setParameter("tp", technicalProduct);
                List<MarketingPermission> result = ParameterizedTypes.list(
                        query.getResultList(), MarketingPermission.class);

                // assert
                assertEquals(3, result.size());
                return null;
            }
        });
    }

    @Test
    public void testNamedQuery_FindForTechnicalService_EmptyResult()
            throws Exception {

        // create another technical product
        final TechnicalProduct tp = runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                TechnicalProduct technicalProduct = TechnicalProducts
                        .createTestData(mgr, provider, 1).get(0);
                mgr.persist(technicalProduct);
                return technicalProduct;
            }
        });

        // run query
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Query query = mgr
                        .createNamedQuery("MarketingPermission.findForTechnicalService");
                query.setParameter("tp", tp);
                List<MarketingPermission> result = ParameterizedTypes.list(
                        query.getResultList(), MarketingPermission.class);

                // assert
                assertEquals(0, result.size());
                return null;
            }
        });
    }

}
