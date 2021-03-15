/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2021
 *
 * <p>Creation Date: 11.03.2021
 *
 * <p>*****************************************************************************
 */
package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;
import org.junit.Test;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;

/** @author goebel */
public class DisplaySettingsIT extends DomainObjectTestBase {
  final long[] userKeys = new long[2];

  @Test
  public void createDisplaySettings() throws Throwable {

    runTX(
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            Organization org =
                Organizations.createOrganization(mgr, OrganizationRoleType.MARKETPLACE_OWNER);
            PlatformUser pu = PlatformUsers.createAdmin(mgr, "admin1", org);
            userKeys[0] = pu.getKey();
            PlatformUser pu2 = PlatformUsers.createAdmin(mgr, "admin2", org);
            userKeys[1] = pu2.getKey();

            DisplaySettings d = new DisplaySettings();
            d.setData("test1");
            d.setUserTKey(userKeys[0]);
            mgr.persist(d);

            DisplaySettings d2 = new DisplaySettings();
            d2.setData("test2");
            d2.setUserTKey(userKeys[1]);
            mgr.persist(d2);

            return null;
          }
        });

    runTX(
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            DisplaySettings ds = new DisplaySettings();
            ds.setUserTKey(userKeys[0]);
            ds = (DisplaySettings) mgr.find(ds);
            assertEquals("test1", ds.getData());
            return null;
          }
        });
  }
}
