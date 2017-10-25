/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: brandstetter                                                      
 *                                                                              
 *  Creation Date: 25.10.2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.taskhandling.payloads;

import java.io.Serializable;

import org.oscm.types.enumtypes.EmailType;

public class MailDefiniton implements Serializable {

    /**
     * A container for storing data used to create an e-mail.
     */
    private static final long serialVersionUID = -5753921456215821568L;

    /**
     * The key of user or organization who will receive this e-mail.
     */
    private Long key;

    /**
     * E-mail type.
     */
    private EmailType type;

    /**
     * Parameters for the e-mail message. Can be 'null'.
     */
    private Object[] params;

    /**
     * The key of marketplace. Can be 'null'.
     */
    private Long marketplaceKey;

    // MailDefiniton for User
    public MailDefiniton(Long userKey, EmailType type, Object[] params,
            Long marketplaceKey) {
        super();
        this.key = userKey;
        this.type = type;
        this.params = params;
        this.marketplaceKey = marketplaceKey;
    }

    public Long getKey() {
        return key;
    }

    public EmailType getType() {
        return type;
    }

    public Object[] getParams() {
        return params;
    }

    public Long getMarketplaceKey() {
        return marketplaceKey;
    }

    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("MarketplaceKey = ").append(marketplaceKey);
        sb.append(", UserKey = ").append(key);
        sb.append(", EmailType = ").append(type);
        sb.append(", Parameters = [");

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                sb.append(params[i].toString()).append(
                        (i + 1 < params.length) ? ", " : "]");
            }
        }
        return sb.toString();
    }
}
