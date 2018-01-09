/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.ui.menu;

import org.oscm.app.vmware.i18n.Messages;

public class LoadbalancerItem implements IMenuItem {

    @Override
    public String getLabel() {
        return Messages.get("en", "ui.config.page.cluster");
    }

    @Override
    public String getLink() {
        return "/vcenter.xhtml";
    }

    @Override
    public int getPosition() {
        return 1;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
