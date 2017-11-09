/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-2-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.recoverPassword;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * This model is used for recover password page
 * 
 * @author Mao
 * 
 */
@ViewScoped
@ManagedBean(name="passwordRecoveryModel")
public class PasswordRecoveryModel {

    private String userId;

    /**
     * Not null for red portal,null for blue portal
     */
    private String marketpalceId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMarketpalceId() {
        return marketpalceId;
    }

    public void setMarketpalceId(String marketpalceId) {
        this.marketpalceId = marketpalceId;
    }

}
