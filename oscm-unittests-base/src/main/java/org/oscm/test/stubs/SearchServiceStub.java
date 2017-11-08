/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import org.oscm.serviceprovisioningservice.local.ProductSearchResult;
import org.oscm.serviceprovisioningservice.local.SearchServiceLocal;
import org.oscm.internal.intf.SearchService;
import org.oscm.internal.types.exception.InvalidPhraseException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.ListCriteria;
import org.oscm.internal.vo.VOServiceListResult;

public class SearchServiceStub implements SearchService, SearchServiceLocal {

    @Override
    public void initIndexForFulltextSearch(boolean force) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceListResult searchServices(String marketplaceId,
            String locale, String searchPhrase) throws InvalidPhraseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceListResult getServicesByCriteria(String marketplaceId,
            String locale, ListCriteria listCriteria) {
        return null;
    }

    @Override
    public ProductSearchResult getServicesByCategory(String marketplaceId,
            String categoryId) throws ObjectNotFoundException {
        return null;
    }

}
