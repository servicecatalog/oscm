/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-1-15                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductReview;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.reviewservice.bean.ReviewServiceLocalBean;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.assembler.ProductFeedbackAssembler;
import org.oscm.internal.assembler.ProductReviewAssembler;
import org.oscm.internal.intf.ReviewService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOServiceFeedback;
import org.oscm.internal.vo.VOServiceReview;

/**
 * Remote facade for creating and retrieving service reviews and ratings.
 * 
 * @author Gao
 * 
 */
@Stateless
@Remote(ReviewService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ReviewServiceBean implements ReviewService {

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    @Inject
    ReviewServiceLocalBean localBean;

    @Override
    public VOServiceReview writeReview(VOServiceReview review)
            throws ValidationException, NonUniqueBusinessKeyException,
            ConcurrentModificationException, OperationNotPermittedException,
            ObjectNotFoundException {
        ArgumentValidator.notNull("review", review);
        ProductReview domainObject = localBean.createOrFindDomainObject(review
                .getKey());
        ProductReviewAssembler.updateProductReview(domainObject, review);
        ProductReview result = localBean.writeReview(domainObject,
                Long.valueOf(review.getProductKey()));
        return ProductReviewAssembler.toVOServiceReview(result);
    }

    @Override
    public void deleteReview(VOServiceReview review)
            throws OperationNotPermittedException, ObjectNotFoundException {
        ArgumentValidator.notNull("review", review);
        localBean.deleteReview(Long.valueOf(review.getKey()));
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public void deleteReviewByMarketplaceOwner(VOServiceReview review,
            String reason) throws OperationNotPermittedException,
            ObjectNotFoundException {
        ArgumentValidator.notNull("review", review);
        ArgumentValidator.notEmptyString("reason", reason);
        localBean.deleteReviewByMarketplaceOwner(Long.valueOf(review.getKey()),
                reason);
    }

    @Override
    public VOServiceFeedback getServiceFeedback(long productKey)
            throws ObjectNotFoundException {
        Product product = dm.getReference(Product.class, productKey);
        VOServiceFeedback feedback = ProductFeedbackAssembler
                .toVOServiceFeedback(product, dm.getCurrentUserIfPresent());
        return feedback;
    }

}
