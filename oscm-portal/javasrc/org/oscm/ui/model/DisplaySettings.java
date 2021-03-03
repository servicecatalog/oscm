/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2021
 *
 * <p>Creation Date: 03.03.2021
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.model;

/** @author goebel */
public class DisplaySettings implements JsonData {

  private static final long serialVersionUID = 1L;

  boolean darkMode = false;
  String primaryColor = "";
  String secondaryColor = "";

  public boolean getDarkMode() {
    return darkMode;
  }

  public void setDarkMode(boolean darkMode) {
    this.darkMode = darkMode;
  }

  public String getPrimaryColor() {
    return primaryColor;
  }

  public void setPrimaryColor(String primaryColor) {
    this.primaryColor = primaryColor;
  }

  public String getSecondaryColor() {
    return secondaryColor;
  }

  public void setSecondaryColor(String secondaryColor) {
    this.secondaryColor = secondaryColor;
  }
}
