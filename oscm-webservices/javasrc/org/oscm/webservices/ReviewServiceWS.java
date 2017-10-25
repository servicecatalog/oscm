/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 20.05.2011                                                      
 *                                                                              
 *  Completion Time: 20.05.2011                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.intf.ReviewService;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOServiceFeedback;
import org.oscm.vo.VOServiceReview;

/**
 * End point facade for review service WS.
 * 
 * @author Mike J&auml;ger
 * 
 */
@WebService(name = "ReviewService", serviceName = "ReviewService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.ReviewService")
public class ReviewServiceWS implements ReviewService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(ReviewServiceWS.class));

    org.oscm.internal.intf.ReviewService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public VOServiceReview writeReview(VOServiceReview review)
            throws ValidationException, NonUniqueBusinessKeyException,
            ConcurrentModificationException, OperationNotPermittedException,
            ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(
                    delegate.writeReview(VOConverter.convertToUp(review)));
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void deleteReview(VOServiceReview review)
            throws OperationNotPermittedException, ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.deleteReview(VOConverter.convertToUp(review));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOServiceFeedback getServiceFeedback(long productKey)
            throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter
                    .convertToApi(delegate.getServiceFeedback(productKey));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void deleteReviewByMarketplaceOwner(VOServiceReview review,
            String reason)
            throws OperationNotPermittedException, ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.deleteReviewByMarketplaceOwner(
                    VOConverter.convertToUp(review), reason);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

}
