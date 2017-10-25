/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                   
 *                                                                              
 *  Creation Date: July 20, 2011                                                      
 *                                                                              
 *  Completion Time: July 20, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.util.HashSet;
import java.util.Set;

import org.oscm.internal.vo.ListCriteria;

/**
 * Wrapper object for list parameters including a result restriction to the
 * specified set of keys.
 * 
 * @author Dirk Bernsau
 * 
 */
public class KeyRestrictedListCriteria extends ListCriteria {

    private Set<Long> keyRestriction;

    private static final long serialVersionUID = -9221875680688075583L;

    public KeyRestrictedListCriteria(Set<Long> keyRestriction) {
        if (keyRestriction != null) {
            this.keyRestriction = new HashSet<Long>(keyRestriction);
        } else {
            this.keyRestriction = null;
        }
    }

    public boolean isRestricted() {
        return keyRestriction != null && keyRestriction.size() > 0;
    }

    public String getRestrictionString() {
        StringBuffer b = new StringBuffer();
        if (isRestricted()) {
            boolean first = true;
            for (Long l : keyRestriction) {
                b.append(first ? "" : ",").append(l.toString());
                first = false;
            }
        }
        return b.toString();
    }
}
