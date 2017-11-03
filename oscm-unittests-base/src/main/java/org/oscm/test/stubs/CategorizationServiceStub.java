/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.ArrayList;
import java.util.List;

import org.oscm.categorizationService.local.CategorizationServiceLocal;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOService;

public class CategorizationServiceStub implements CategorizationService,
        CategorizationServiceLocal {

    @Override
    public void deassignAllCategories(CatalogEntry catalogEntry) {
        // nothing
    }

    @Override
    public boolean updateAssignedCategories(CatalogEntry catalogEntry,
            List<VOCategory> categories) {
        return false;
    }

    @Override
    public List<VOCategory> getCategories(String marketplaceId, String locale) {
        return new ArrayList<VOCategory>();
    }

    @Override
    public void saveCategories(List<VOCategory> toBeSaved,
            List<VOCategory> toBeDeleted, String locale)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, ValidationException,
            NonUniqueBusinessKeyException {
    }

    @Override
    public List<VOService> getServicesForCategory(long categoryKey) {
        return new ArrayList<VOService>();
    }

    @Override
    public void verifyCategoriesUpdated(List<VOCategory> categories)
            throws ConcurrentModificationException, ObjectNotFoundException {
    }
}
