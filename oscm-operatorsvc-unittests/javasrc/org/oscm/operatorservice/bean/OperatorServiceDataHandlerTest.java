/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.03.2011                                                      
 *                                                                              
 *  Completion Time: 14.03.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.test.stubs.DataServiceStub;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Test class for the OperatorServiceDataHandler
 * 
 * @author Mike J&auml;ger
 * 
 */
public class OperatorServiceDataHandlerTest {

    private DataServiceStub ds;
    private Organization source;
    private Organization target;

    private OrganizationReference searchTemplate;
    private OrganizationReference dataServiceResult;

    @Before
    public void setUp() throws Exception {
        source = new Organization();
        source.setKey(1);
        target = new Organization();
        target.setKey(2);

        ds = new DataServiceStub() {
            @Override
            public DomainObject<?> find(DomainObject<?> idobj) {
                searchTemplate = (OrganizationReference) idobj;
                return dataServiceResult;
            }

            @Override
            public void flush() {
            }

            @Override
            public void persist(DomainObject<?> obj)
                    throws NonUniqueBusinessKeyException {
                obj.setKey(500);
            }
        };
    }

    @Test(expected = NullPointerException.class)
    public void testFindOrCreateOrganizationReference_NullSource()
            throws Exception {
        OperatorServiceDataHandler.findOrCreateOrganizationReference(null,
                new Organization(),
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER, ds);
    }

    @Test(expected = NullPointerException.class)
    public void testFindOrCreateOrganizationReference_NullTarget()
            throws Exception {
        OperatorServiceDataHandler.findOrCreateOrganizationReference(
                new Organization(), null,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER, ds);
    }

    @Test(expected = NullPointerException.class)
    public void testFindOrCreateOrganizationReference_NullOrgRefType()
            throws Exception {
        OperatorServiceDataHandler.findOrCreateOrganizationReference(
                new Organization(), new Organization(), null, ds);
    }

    @Test(expected = NullPointerException.class)
    public void testFindOrCreateOrganizationReference_NullDataService()
            throws Exception {
        OperatorServiceDataHandler.findOrCreateOrganizationReference(
                new Organization(), new Organization(),
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER, null);
    }

    @Test
    public void testFindOrCreateOrganizationReference_AlreadyExisting()
            throws Exception {
        OrganizationReference orgRefResult = new OrganizationReference(source,
                target, OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
        orgRefResult.setKey(5);
        dataServiceResult = orgRefResult;
        OrganizationReference result = OperatorServiceDataHandler
                .findOrCreateOrganizationReference(
                        source,
                        target,
                        OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER,
                        ds);
        assertNotNull(searchTemplate);
        assertEquals(1, searchTemplate.getSourceKey());
        assertEquals(2, searchTemplate.getTargetKey());
        assertEquals(OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER,
                searchTemplate.getReferenceType());
        assertNotNull(result);
        assertEquals(5, result.getKey()); // not newly created
    }

    @Test
    public void testFindOrCreateOrganizationReference_NonExisting()
            throws Exception {
        OrganizationReference result = OperatorServiceDataHandler
                .findOrCreateOrganizationReference(
                        source,
                        target,
                        OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER,
                        ds);
        assertNotNull(result);
        assertEquals(1, result.getSourceKey());
        assertEquals(2, result.getTargetKey());
        assertEquals(OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER,
                result.getReferenceType());
        assertEquals(500, result.getKey());
        assertFalse(source.getTargets().isEmpty());
        assertFalse(target.getSources().isEmpty());
    }

}
