/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2011-05-12                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOServiceFeedback;
import org.oscm.internal.vo.VOServiceReview;

/**
 * Remote interface for creating and retrieving service reviews and ratings.
 * 
 */
@Remote
public interface ReviewService {

    /**
     * Creates or updates a review for a service. After subscribing to a
     * service, a customer is allowed to publish his opinion on it. A review
     * consists of a comment and a rating. The rating is a value between 1 and
     * 5; at a user interface, it is typically represented by stars.
     * <p>
     * Required role: any user role in the customer organization to create or
     * update one's own review; administrator of the customer organization to
     * update the reviews of other users
     * 
     * @param review
     *            the value object containing the data of the review
     * @return the stored data of the review
     * @throws ValidationException
     *             if the input values do not meet the constraints
     * @throws NonUniqueBusinessKeyException
     *             if the review to be created already exists
     * @throws ConcurrentModificationException
     *             if an existing review is changed by another user in the time
     *             between reading and writing it
     * @throws OperationNotPermittedException
     *             if the calling user is not the owner of the review or an
     *             administrator of the organization that subscribed to the
     *             service
     * @throws ObjectNotFoundException
     *             if the review is not found
     */

    public VOServiceReview writeReview(VOServiceReview review)
            throws ValidationException, NonUniqueBusinessKeyException,
            ConcurrentModificationException, ObjectNotFoundException,
            OperationNotPermittedException;

    /**
     * Deletes the specified review for a service.
     * <p>
     * Required role: any user role in an organization to delete one's own
     * reviews; administrator of the organization to delete the reviews of other
     * users
     * 
     * @param review
     *            the value object identifying the review to delete
     * @throws OperationNotPermittedException
     *             if the calling user is not the owner of the review or an
     *             administrator of the organization that subscribed to the
     *             service
     * @throws ObjectNotFoundException
     *             if the review is not found
     */

    public void deleteReview(VOServiceReview review)
            throws OperationNotPermittedException, ObjectNotFoundException;

    /**
     * Deletes the specified review for a service. The creator of the review is
     * informed by mail about the deletion and its reason. Be aware that the
     * deletion is not aborted if the mail cannot be sent.
     * <p>
     * Required role: marketplace manager of the organization owning the
     * marketplace where the review was created
     * 
     * @param review
     *            the value object identifying the review to delete.
     * @param reason
     *            the reason why the review is deleted by the marketplace
     *            manager
     * @throws OperationNotPermittedException
     *             if the calling user is not a marketplace manager of the
     *             marketplace owner organization
     * @throws ObjectNotFoundException
     *             if the review is not found
     */

    public void deleteReviewByMarketplaceOwner(VOServiceReview review,
            String reason) throws OperationNotPermittedException,
            ObjectNotFoundException;

    /**
     * Returns the customer feedback for the specified service. The feedback
     * consists of the reviews in which customers provide comments and ratings
     * of the service.
     * <p>
     * Required role: none
     * 
     * @param serviceKey
     *            the numeric key of the service
     * @return the customer feedback for the service
     * @throws ObjectNotFoundException
     *             if the service is not found
     */

    public VOServiceFeedback getServiceFeedback(long serviceKey)
            throws ObjectNotFoundException;
}
