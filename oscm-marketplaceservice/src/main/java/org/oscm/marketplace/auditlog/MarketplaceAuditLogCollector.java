/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.auditlog;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.auditlog.AuditLogData;
import org.oscm.auditlog.AuditLogParameter;
import org.oscm.auditlog.BESAuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Product;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.AuditLoggingEnabled;
import org.oscm.internal.vo.VOCategory;

@Stateless
@Interceptors(AuditLoggingEnabled.class)
public class MarketplaceAuditLogCollector {

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    public void setServiceAsPublic(DataService ds, Product product,
            boolean servicePublic) {

        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                MarketplaceAuditLogOperation.SET_SERVICE_AS_PUBLIC, product);

        logEntry.addParameter(AuditLogParameter.SERVICE_PUBLIC, new Boolean(
                servicePublic).toString());

        AuditLogData.add(logEntry);
    }

    public void assignToMarketPlace(DataService ds, Product product,
            String marketplaceId, String marketplaceName) {
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                MarketplaceAuditLogOperation.ASSIGN_SERVICE_TO_MARKETPLACE,
                product);
        logEntry.addParameter(AuditLogParameter.MARKETPLACE_ID, marketplaceId);
        logEntry.addParameter(AuditLogParameter.MARKETPLACE_NAME,
                marketplaceName);

        AuditLogData.add(logEntry);
    }

    public void assignCategories(DataService ds, Product product,
            List<VOCategory> categorieslist) {
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                MarketplaceAuditLogOperation.ASSIGN_CATAGORIES, product);

        StringBuffer idList = new StringBuffer();
        if (categorieslist != null && categorieslist.size() > 0) {
            for (int i = 0; i < categorieslist.size(); i++) {
                if (i == 0) {
                    idList.append(categorieslist.get(i).getCategoryId());
                } else {
                    idList.append(",").append(
                            categorieslist.get(i).getCategoryId());
                }
            }
        }

        logEntry.addParameter(AuditLogParameter.CATEGORIES_ID,
                idList.toString());

        AuditLogData.add(logEntry);
    }

    private BESAuditLogEntry createAuditLogEntry(DataService ds,
            MarketplaceAuditLogOperation operation, Product product) {
        BESAuditLogEntry logEntry = new BESAuditLogEntry(ds,
                operation.getOperationId(), operation.toString(),
                operation.getParameters());

        logEntry.addProduct(product, localizer);
        return logEntry;
    }
}
