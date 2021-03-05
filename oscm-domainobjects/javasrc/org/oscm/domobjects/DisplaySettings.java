/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2021
 *
 * <p>Creation Date: 03.03.2021
 *
 * <p>*****************************************************************************
 */
package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;

/** @author goebel */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userTKey"}))
@NamedQueries({
  @NamedQuery(
      name = "DisplaySettings.findByBusinessKey",
      query = "SELECT d FROM DisplaySettings d WHERE d.dataContainer.userTKey = " + ":userTKey")
})
@BusinessKey(attributes = {"userTKey"})
public class DisplaySettings extends DomainObjectWithVersioning<DisplaySettingsData> {

  /** */
  private static final long serialVersionUID = 935271439786172232L;

  public DisplaySettings() {
    super();
    dataContainer = new DisplaySettingsData();
  }

  public void setUserTKey(long userTKey) {
    dataContainer.setUserTKey(userTKey);
  }

  public long getUserTKey() {
    return dataContainer.getUserTKey();
  }

  public void setData(String data) {
    dataContainer.setData(data);
  }

  public String getData() {
    return dataContainer.getData();
  }
}
