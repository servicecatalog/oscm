/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.review;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.oscm.domobjects.ProductReview;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.reviewservice.bean.ReviewServiceLocalBean;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.assembler.POServiceReviewAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Implementation of top layer service for creating and retrieving service
 * reviews and ratings.
 * 
 * @author Gao
 * 
 */
@Stateless
@Remote(ReviewInternalService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ReviewInternalServiceBean implements ReviewInternalService {

    @Inject
    ReviewServiceLocalBean reviewService;

    @Override
    public POServiceReview writeReview(POServiceReview poReview)
            throws ValidationException, NonUniqueBusinessKeyException,
            ConcurrentModificationException, ObjectNotFoundException,
            OperationNotPermittedException {
        ArgumentValidator.notNull("poReview", poReview);
        ProductReview domainObject = reviewService
                .createOrFindDomainObject(poReview.getKey());
        POServiceReviewAssembler.updateProductReview(domainObject, poReview);
        ProductReview result = reviewService.writeReview(domainObject,
                Long.valueOf(poReview.getProductKey()));
        return POServiceReviewAssembler.toPOServiceReview(result);
    }

    @Override
    public void deleteReview(POServiceReview poReview)
            throws OperationNotPermittedException, ObjectNotFoundException {
        ArgumentValidator.notNull("poReview", poReview);
        reviewService.deleteReview(Long.valueOf(poReview.getKey()));
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public void deleteReviewByMarketplaceOwner(POServiceReview poReview,
            String reason) throws OperationNotPermittedException,
            ObjectNotFoundException {
        ArgumentValidator.notNull("poReview", poReview);
        ArgumentValidator.notEmptyString("reason", reason);
        reviewService.deleteReviewByMarketplaceOwner(
                Long.valueOf(poReview.getKey()), reason);

    }

}
