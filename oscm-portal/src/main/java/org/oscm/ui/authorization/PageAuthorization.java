/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-5-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.authorization;

import java.util.Map;

/**
 * The page authorization for user
 * 
 * @author gaowenxin
 * 
 */
public class PageAuthorization implements UIStatus {

    private final String currentPageLink;
    private final Condition condition;
    private final String currentPageId;
    private final PageAuthorizationBuilder builder;

    public PageAuthorization(String currentPageLink, Condition condition,
            String currentPageId, PageAuthorizationBuilder builder) {
        this.currentPageLink = currentPageLink;
        this.condition = condition;
        this.currentPageId = currentPageId;
        this.builder = builder;
    }

    public boolean isAuthorized() {
        return condition.eval() & !isHidden(currentPageId);
    }

    @Override
    public String getCurrentPageLink() {
        return currentPageLink;
    }

    @Override
    public boolean isHidden(String id) {
        final Map<String, Boolean> tmpSet = builder.getHiddenUIElements();
        return tmpSet.containsKey(id);
    }
}
