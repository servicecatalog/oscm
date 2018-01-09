/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.ui.menu;

public interface IMenuItem {

    String getLabel();

    String getLink();

    int getPosition();

    boolean isEnabled();
}
