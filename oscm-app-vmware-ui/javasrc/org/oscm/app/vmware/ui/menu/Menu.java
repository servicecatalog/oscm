/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.ui.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

@ManagedBean
public class Menu {

    @Inject
    private Instance<IMenuItem> itemProvider;

    public List<IMenuItem> getItems() {
        List<IMenuItem> result = new ArrayList<>();
        for (IMenuItem iMenuItem : itemProvider) {
            if (iMenuItem.isEnabled()) {
                result.add(iMenuItem);
            }
        }
        Collections.sort(result, new Comparator<IMenuItem>() {

            @Override
            public int compare(IMenuItem o1, IMenuItem o2) {
                return o1.getPosition() - o2.getPosition();
            }
        });
        return result;
    }
}
