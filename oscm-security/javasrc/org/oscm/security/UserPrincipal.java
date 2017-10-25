/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                 
 *  Creation Date: 19.07.2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.security;

import java.security.Principal;

/**
 * @author stavreva
 *
 */

public class UserPrincipal implements Principal {

    private String name;

    public UserPrincipal(String name) {
        super();
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
