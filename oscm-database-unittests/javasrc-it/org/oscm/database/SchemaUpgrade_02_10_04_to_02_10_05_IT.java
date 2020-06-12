/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 12.06.20 11:54
 *
 * <p>****************************************************************************
 */
package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_10_04_to_02_10_05_IT extends SchemaUpgradeTestBase {

  public SchemaUpgrade_02_10_04_to_02_10_05_IT() {
    super(new DatabaseVersionInfo(2, 10, 04), new DatabaseVersionInfo(2, 10, 05));
  }

  @Override
  protected URL getSetupDataset() {
    return getClass().getResource("/setup_02_10_04_to_02_10_05.xml");
  }

  @Override
  protected URL getExpectedDataset() {
    return getClass().getResource("/expected_02_10_04_to_02_10_05.xml");
  }
}
