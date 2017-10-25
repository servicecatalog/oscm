/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                 
 *  Creation Date: 19.07.2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.security;

/**
 * @author stavreva
 *
 */
import java.security.Principal;

public class RolePrincipal implements Principal {
  
  private String name;
  
  public RolePrincipal(String name) {
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
