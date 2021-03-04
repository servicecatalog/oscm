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

import java.io.Serializable;

import javax.persistence.Column;

/** @author goebel */
public class DisplaySettingsData extends DomainDataContainer implements Serializable {

  /** */
  private static final long serialVersionUID = -8837450327201421421L;

  @Column(nullable = false)
  private long userTKey;

  @Column(nullable = false)
  private String data;

  public void setData(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }

  public void setUserTKey(long userTKey) {
    this.userTKey = userTKey;
  }

  public long getUserTKey() {
    return this.userTKey;
  }
}
