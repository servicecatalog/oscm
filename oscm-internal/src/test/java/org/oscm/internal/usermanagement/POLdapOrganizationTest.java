/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class POLdapOrganizationTest {

    private POLdapOrganization org1;
    private POLdapOrganization equalOrg;

    @Before
    public void setup() {
        org1 = new POLdapOrganization(5, 9, "orgName1", "orgId1");
        equalOrg = new POLdapOrganization(org1.getKey(), org1.getVersion(),
                org1.getName(), org1.getIdentifier());
    }

    @Test
    public void equals_Null() {
        assertFalse(org1 == null);
    }

    @Test
    public void equals_Same() {
        assertTrue(org1.equals(org1));
    }

    @Test
    public void equals_Identical() {
        assertTrue(org1.equals(equalOrg));
    }

    @Test
    public void equals_SameKey() {
        POLdapOrganization org2 = new POLdapOrganization(org1.getKey(), 1,
                "someName", "someId");
        assertTrue(org1.equals(org2));
    }

    @Test
    public void equals_OtherKey() {
        POLdapOrganization org2 = new POLdapOrganization(org1.getKey() + 1, 1,
                "someName", "someId");
        assertFalse(org1.equals(org2));
    }

    @Test
    public void hashCode_Same() {
        assertEquals(org1.hashCode(), org1.hashCode());
    }

    @Test
    public void hashCode_Equal() {
        assertEquals(org1.hashCode(), equalOrg.hashCode());
    }

    @Test
    public void hashCode_Other() {
        equalOrg.setKey(equalOrg.getKey() + 2);
        assertFalse(org1.hashCode() == equalOrg.hashCode());
    }

}
