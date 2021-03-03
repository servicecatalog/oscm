/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>Creation Date: 22.07.19 11:58
 *
 * <p>****************************************************************************
 */
package org.oscm.database;

import java.net.URL;
import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_10_05_to_02_11_00_IT extends SchemaUpgradeTestBase {

  public SchemaUpgrade_02_10_05_to_02_11_00_IT() {
    super(new DatabaseVersionInfo(2, 10, 05), new DatabaseVersionInfo(2, 11, 0));
  }

  @Override
  protected URL getSetupDataset() {
    return getClass().getResource("/setup_02_10_05_to_02_11_00.xml");
  }

  @Override
  protected URL getExpectedDataset() {
    return getClass().getResource("/expected_02_10_05_to_02_11_00.xml");
  }
}
