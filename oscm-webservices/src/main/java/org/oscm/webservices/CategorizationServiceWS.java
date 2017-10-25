/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 20.02.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices;

import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOCollectionConverter;
import org.oscm.intf.CategorizationService;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOCategory;
import org.oscm.vo.VOService;

/**
 * @author cheld
 */
@WebService(name = "CategorizationService", serviceName = "CategorizationService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.CategorizationService")
public class CategorizationServiceWS implements CategorizationService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(BillingServiceWS.class));

    DataService ds;
    org.oscm.internal.intf.CategorizationService delegate;
    WebServiceContext wsContext;

    @Override
    public List<VOCategory> getCategories(String marketplaceId, String local) {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getCategories(marketplaceId, local),
                org.oscm.vo.VOCategory.class);
    }

    @Override
    public void saveCategories(List<VOCategory> toBeSaved,
            List<VOCategory> toBeDeleted, String local)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, ValidationException,
            NonUniqueBusinessKeyException {
        try {
            delegate.saveCategories(
                    VOCollectionConverter.convertList(toBeSaved,
                            org.oscm.internal.vo.VOCategory.class),
                    VOCollectionConverter.convertList(toBeDeleted,
                            org.oscm.internal.vo.VOCategory.class),
                    local);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOService> getServicesForCategory(long categoryKey) {
        return VOCollectionConverter.convertList(
                delegate.getServicesForCategory(categoryKey),
                org.oscm.vo.VOService.class);
    }
}
