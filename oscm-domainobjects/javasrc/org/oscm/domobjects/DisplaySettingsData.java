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
  private String userId;

  @Column(nullable = false)
  private String data;

  public void setData(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return this.userId;
  }
}
