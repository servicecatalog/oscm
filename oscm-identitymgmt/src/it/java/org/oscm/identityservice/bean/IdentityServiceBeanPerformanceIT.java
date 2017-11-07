/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 22.11.2011                                                      
 *                                                                              
 *  Completion Time: 22.11.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;

/**
 * @author weiser
 * 
 */
@Ignore
public class IdentityServiceBeanPerformanceIT extends EJBTestBase {

    private DataService ds;
    private IdentityService is;
    private long userKey;

    @Override
    protected void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        container.enableInterfaceMocking(true);
        container.login("1");
        container.addBean(new DataServiceBean());
        container.addBean(new IdentityServiceBean());

        ds = container.get(DataService.class);
        is = container.get(IdentityService.class);

        userKey = runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                createOrganizationRoles(ds);
                createUserRoles(ds);

                Organization org = Organizations.createOrganization(ds,
                        OrganizationRoleType.CUSTOMER,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                final PlatformUser user = Organizations.createUserForOrg(ds,
                        org, true, "admin");
                return Long.valueOf(user.getKey());
            }
        }).longValue();
    }

    @Test
    public void getUsers() throws Exception {
        final int count = 100;
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                PlatformUser user = ds.getReference(PlatformUser.class,
                        userKey);
                for (int i = 0; i < count; i++) {
                    PlatformUser usr = Organizations.createUserForOrg(ds,
                            user.getOrganization(), ((i % 2) != 0),
                            "user_" + i);
                    if ((i % 5) != 0) {
                        PlatformUsers.grantRoles(ds, usr,
                                UserRoleType.SERVICE_MANAGER);
                    }
                    if ((i % 7) != 0) {
                        PlatformUsers.grantRoles(ds, usr,
                                UserRoleType.TECHNOLOGY_MANAGER);
                    }
                }
                return null;
            }
        });
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);
        long start = System.currentTimeMillis();
        List<VOUserDetails> list = is.getUsersForOrganization();
        long end = System.currentTimeMillis();
        long time = end - start;
        Assert.assertEquals(count + 1, list.size());
        Assert.assertTrue("getting 100 users took more than 300ms.",
                time < 300);
    }
}
