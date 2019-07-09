/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019
 *                                                                              
 *  Creation Date: Jul 9, 2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import javax.servlet.http.HttpServletRequest;

import org.oscm.ui.common.Constants;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

public class OidcTokenHandler {

  private HttpServletRequest httpRequest;

  public OidcTokenHandler(HttpServletRequest httpRequest) {
    this.httpRequest = httpRequest;
  }

  public String getUserId() {

    String idToken = (String) httpRequest.getSession().getAttribute(Constants.SESS_ATTR_ID_TOKEN);
    DecodedJWT decodedToken = JWT.decode(idToken);
    String userId = decodedToken.getClaim("unique_name").asString();

    return userId;
  }
}
