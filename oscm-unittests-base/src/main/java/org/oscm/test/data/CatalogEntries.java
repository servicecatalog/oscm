/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.data;

import java.math.BigDecimal;
import java.util.Date;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.CatalogEntryHistory;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

public class CatalogEntries {
    public static CatalogEntry create(DataService mgr, Marketplace marketplace,
            Product product, boolean visibleInCatalog)
            throws NonUniqueBusinessKeyException {
        CatalogEntry catalogEntry = new CatalogEntry();
        catalogEntry.setMarketplace(marketplace);
        catalogEntry.setProduct(product);
        catalogEntry.setVisibleInCatalog(visibleInCatalog);

        if (product.getType().equals(ServiceType.TEMPLATE)
                && product.getVendor().getOperatorPriceModel() != null) {
            Organization vendor = (Organization) mgr.find(product.getVendor());
            RevenueShareModel operatorPriceModelCopy = vendor
                    .getOperatorPriceModel().copy();
            catalogEntry.setOperatorPriceModel(operatorPriceModelCopy);
            mgr.persist(operatorPriceModelCopy);
        }

        mgr.persist(catalogEntry);
        return catalogEntry;
    }

    public static CatalogEntry createWithBrokerShare(DataService ds,
            Marketplace marketplace, Product product, BigDecimal percentage)
            throws NonUniqueBusinessKeyException {

        RevenueShareModel revenueShare = new RevenueShareModel();
        revenueShare.setRevenueShare(percentage);
        revenueShare
                .setRevenueShareModelType(RevenueShareModelType.BROKER_REVENUE_SHARE);
        ds.persist(revenueShare);

        CatalogEntry catalogEntry = new CatalogEntry();
        catalogEntry.setMarketplace(marketplace);
        catalogEntry.setProduct(product);
        catalogEntry.setVisibleInCatalog(true);
        catalogEntry.setBrokerPriceModel(revenueShare);
        ds.persist(catalogEntry);

        return catalogEntry;
    }

    public static CatalogEntry createWithResellerShare(DataService ds,
            Marketplace marketplace, Product product, BigDecimal percentage)
            throws NonUniqueBusinessKeyException {

        RevenueShareModel revenueShare = new RevenueShareModel();
        revenueShare.setRevenueShare(percentage);
        revenueShare
                .setRevenueShareModelType(RevenueShareModelType.RESELLER_REVENUE_SHARE);
        ds.persist(revenueShare);

        CatalogEntry catalogEntry = new CatalogEntry();
        catalogEntry.setMarketplace(marketplace);
        catalogEntry.setProduct(product);
        catalogEntry.setVisibleInCatalog(true);
        catalogEntry.setResellerPriceModel(revenueShare);
        ds.persist(catalogEntry);

        return catalogEntry;
    }

    public static CatalogEntry create(DataService ds, Marketplace marketplace,
            Product product) throws NonUniqueBusinessKeyException {

        CatalogEntry catalogEntry = new CatalogEntry();
        catalogEntry.setMarketplace(marketplace);
        catalogEntry.setProduct(product);
        catalogEntry.setVisibleInCatalog(true);
        if (product.getType() == ServiceType.TEMPLATE) {
            RevenueShareModel revenueShare = new RevenueShareModel();
            if (product.getVendor().getOperatorPriceModel() != null) {
                revenueShare.setRevenueShare(product.getVendor()
                        .getOperatorPriceModel().getRevenueShare());
            } else {
                revenueShare.setRevenueShare(BigDecimal.ZERO);
            }
            revenueShare
                    .setRevenueShareModelType(RevenueShareModelType.OPERATOR_REVENUE_SHARE);
            ds.persist(revenueShare);
            catalogEntry.setOperatorPriceModel(revenueShare);
        }
        ds.persist(catalogEntry);

        return catalogEntry;
    }

    public static CatalogEntry updateMarketplace(DataService ds,
            CatalogEntry catalogEntry, Marketplace marketplace)
            throws NonUniqueBusinessKeyException {

        catalogEntry.setMarketplace(marketplace);
        ds.persist(catalogEntry);
        return catalogEntry;
    }

    public static CatalogEntryHistory createCatalogEntryHistory(DataService ds,
            final long objKey, final Date modDate, final int version,
            final ModificationType modificationType, final Long productobjkey,
            final Long marketplaceobjkey, final Long brokerpricemodelobjkey,
            final Long resellerpricemodelobjkey,
            final Long operatorpricemodelobjkey) throws Exception {

        CatalogEntryHistory ceh = new CatalogEntryHistory();
        ceh.setObjKey(objKey);
        ceh.setInvocationDate(new Date());
        ceh.setObjVersion(version);
        ceh.setModdate(modDate);
        ceh.setModtype(modificationType);
        ceh.setModuser("moduser");
        ceh.setProductObjKey(productobjkey);
        ceh.setMarketplaceObjKey(marketplaceobjkey);
        ceh.setBrokerPriceModelObjKey(brokerpricemodelobjkey);
        ceh.setResellerPriceModelObjKey(resellerpricemodelobjkey);
        ceh.setOperatorPriceModelObjKey(operatorpricemodelobjkey);
        ds.persist(ceh);
        return ceh;
    }

}
