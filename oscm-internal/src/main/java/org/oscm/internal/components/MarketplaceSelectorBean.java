/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.vo.VOMarketplace;

@Stateless
@Local(MarketplaceSelector.class)
public class MarketplaceSelectorBean implements MarketplaceSelector {

    @EJB
    MarketplaceService marketplaceService;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB
    MarketplaceServiceLocal marketplaceServiceLocal;

    public List<POMarketplace> getMarketplaces() {
        Set<VOMarketplace> mpls = loadMarketplaces();
        List<POMarketplace> marketplaces = new ArrayList<POMarketplace>();
        if (mpls != null) {
            for (VOMarketplace mp : mpls) {
                marketplaces.add(convertPO(mp));
            }
        }
        return marketplaces;
    }

    Set<VOMarketplace> loadMarketplaces() {
        Set<VOMarketplace> marketplaces = new HashSet<VOMarketplace>();
        marketplaces.addAll(marketplaceService.getMarketplacesOwned());
        return marketplaces;
    }

    private POMarketplace convertPO(VOMarketplace vo) {
        return new POMarketplace(vo.getMarketplaceId(), createDisplayLabel(vo));
    }

    String createDisplayLabel(VOMarketplace marketplace) {
        return marketplace.getName() + " (" + marketplace.getMarketplaceId()
                + ")";
    }

    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public List<POMarketplace> getMarketplacesForPublishing() {
        List<POMarketplace> poMarketplacesList = new ArrayList<POMarketplace>();
        LocalizerFacade facade = new LocalizerFacade(localizer, dm
                .getCurrentUser().getLocale());

        for (Marketplace mp : marketplaceServiceLocal
                .getMarketplacesForSupplier()) {
            poMarketplacesList.add(toPOMarketplace(mp, facade));
        }

        return poMarketplacesList;
    }

    static POMarketplace toPOMarketplace(Marketplace marketplace,
            LocalizerFacade localizerFacade) {
        POMarketplace poMarketplace = new POMarketplace(
                marketplace.getMarketplaceId(), localizerFacade.getText(
                        marketplace.getKey(),
                        LocalizedObjectTypes.MARKETPLACE_NAME));
        poMarketplace.setKey(marketplace.getKey());
        poMarketplace.setVersion(marketplace.getVersion());
        return poMarketplace;
    }
}
