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
public class DisplaySettings extends JsonData {

  private static final long serialVersionUID = 1L;

  boolean darkMode = false;
  String primaryColor = "";
  String fontColor = "";
  String navbarColor = "";
  String navbarLinkColor = "";
  String inputColor = "";

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

  public String getFontColor() {
    return fontColor;
  }

  public void setFontColor(String fontColor) {
    this.fontColor = fontColor;
  }

  public String getNavbarColor() {
    return navbarColor;
  }

  public void setNavbarColor(String navbarColor) {
    this.navbarColor = navbarColor;
  }

  public String getNavbarLinkColor() {
    return navbarLinkColor;
  }

  public void setNavbarLinkColor(String navbarLinkColor) {
    this.navbarLinkColor = navbarLinkColor;
  }

  public String getInputColor() {
    return inputColor;
  }

  public void setInputColor(String inputColor) {
    this.inputColor = inputColor;
  }
}
