/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 7, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.usergroupservice.auditlog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;

import org.oscm.auditlog.AuditLogData;
import org.oscm.auditlog.AuditLogParameter;
import org.oscm.auditlog.BESAuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.interceptor.AuditLoggingEnabled;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author zhaoh.fnst
 * 
 */
@Stateless
@LocalBean
@Interceptors(AuditLoggingEnabled.class)
public class UserGroupAuditLogCollector {

    private static final String DEFAULTLOCALE = "en";

    public void accessToServices(DataService ds,
            UserGroupAuditLogOperation operation, UserGroup group,
            List<Product> products, String marketplaceId)
            throws ObjectNotFoundException {
        Map<Long, String> productNameMap = getProductName(ds, products);
        for (Product product : products) {
            BESAuditLogEntry logEntry = createAuditLogEntry(ds, operation);
            logEntry.addParameter(AuditLogParameter.GROUP, group.getName());
            logEntry.addParameter(AuditLogParameter.MARKETPLACE_ID,
                    marketplaceId);
            logEntry.addParameter(AuditLogParameter.MARKETPLACE_NAME,
                    getMarketplaceName(ds, marketplaceId));
            logEntry.addParameter(AuditLogParameter.SERVICE_ID, product
                    .getTemplateOrSelf().getProductId());
            logEntry.addParameter(AuditLogParameter.SERVICE_NAME,
                    productNameMap.get(Long.valueOf(product.getKey())));
            logEntry.addParameter(AuditLogParameter.SELLER_ID, product
                    .getVendor().getOrganizationId());
            AuditLogData.add(logEntry);
        }
    }

    @ExcludeClassInterceptors
    public void assignUserToGroups(DataService ds, Collection<UserGroup> groups,
            PlatformUser user) {
        List<PlatformUser> users = new ArrayList<PlatformUser>();
        users.add(user);
        for (UserGroup userGroup : groups) {
            assignUsersToGroup(ds, userGroup, users);
        }
    }

    public void assignUsersToGroup(DataService ds, UserGroup group,
            List<PlatformUser> users) {
        if (group.isDefault()) {
            return;
        }
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                UserGroupAuditLogOperation.ASSIGN_USER_TO_GROUP);
        String usersId = "";
        for (PlatformUser user : users) {
            usersId += "," + user.getUserId();
        }
        if (!users.isEmpty()) {
            usersId = usersId.substring(1);
        }

        logEntry.addParameter(AuditLogParameter.USER, usersId);
        logEntry.addParameter(AuditLogParameter.GROUP, group.getName());
        AuditLogData.add(logEntry);
    }

    @ExcludeClassInterceptors
    public void removeUserFromGroups(DataService ds, List<UserGroup> groups,
            PlatformUser user) {
        List<PlatformUser> users = new ArrayList<PlatformUser>();
        users.add(user);
        for (UserGroup group : groups) {
            removeUsersFromGroup(ds, group, users);
        }
    }

    public void removeUsersFromGroup(DataService ds, UserGroup group,
            List<PlatformUser> users) {
        if (group.isDefault()) {
            return;
        }
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                UserGroupAuditLogOperation.REMOVE_USER_FROM_GROUP);

        String usersId = "";
        if (null != users && !users.isEmpty()) {
            for (PlatformUser user : users) {
                usersId += "," + user.getUserId();
            }
            usersId = usersId.substring(1);
        }

        logEntry.addParameter(AuditLogParameter.USER, usersId);
        logEntry.addParameter(AuditLogParameter.GROUP, group.getName());
        AuditLogData.add(logEntry);
    }

    private BESAuditLogEntry createAuditLogEntry(DataService ds,
            UserGroupAuditLogOperation operation) {
        BESAuditLogEntry logEntry = new BESAuditLogEntry(ds,
                operation.getOperationId(), operation.toString(),
                operation.getParameters());
        return logEntry;
    }

    String getMarketplaceName(DataService dm, String marketplaceId)
            throws ObjectNotFoundException {
        LocalizedResource resource = new LocalizedResource();
        Marketplace marketplace = loadMarketplace(dm, marketplaceId);
        resource.setObjectKey(marketplace.getKey());
        resource.setLocale(dm.getCurrentUser().getLocale());
        resource.setObjectType(LocalizedObjectTypes.MARKETPLACE_NAME);
        LocalizedResource result;
        result = (LocalizedResource) dm.find(resource);
        if (null == result) {
            resource.setLocale(DEFAULTLOCALE);
            result = (LocalizedResource) dm.find(resource);
        }
        if (null == result) {
            return "";
        }

        return result.getValue();
    }

    private Marketplace loadMarketplace(DataService dm, String marketplaceId)
            throws ObjectNotFoundException {
        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId(marketplaceId);
        Marketplace result;
        try {
            result = (Marketplace) dm.getReferenceByBusinessKey(marketplace);
        } catch (ObjectNotFoundException ex) {
            ex.setMessageParams(new String[] { marketplaceId });
            throw ex;
        }
        return result;
    }

    Map<Long, String> getProductName(DataService dm, List<Product> products) {
        Map<Long, String> map = new HashMap<Long, String>();
        for (Product prod : products) {
            LocalizedResource resource = new LocalizedResource();
            resource.setObjectKey(prod.getTemplateOrSelf().getKey());
            resource.setLocale(dm.getCurrentUser().getLocale());
            resource.setObjectType(LocalizedObjectTypes.PRODUCT_MARKETING_NAME);
            LocalizedResource result;
            result = (LocalizedResource) dm.find(resource);
            if (null == result) {
                resource.setLocale(DEFAULTLOCALE);
                result = (LocalizedResource) dm.find(resource);
            }
            if (null == result) {
                map.put(Long.valueOf(prod.getKey()), "");
            } else {
                map.put(Long.valueOf(prod.getKey()), result.getValue());
            }
        }
        return map;
    }
}
