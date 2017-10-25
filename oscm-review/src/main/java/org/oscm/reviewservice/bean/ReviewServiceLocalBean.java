/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reviewservice.bean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductFeedback;
import org.oscm.domobjects.ProductReview;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.reviewservice.dao.ProductReviewDao;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.exceptions.InvalidUserSession;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;

/**
 * No interface view bean for review service.
 * 
 * @author Gao
 * 
 */
@Stateless
@LocalBean
public class ReviewServiceLocalBean {

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = CommunicationServiceLocal.class)
    CommunicationServiceLocal cs;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @EJB(beanInterface = ProductReviewDao.class)
    ProductReviewDao productReviewDao;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ReviewServiceLocalBean.class);

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
     *            the object containing the data of the review
     * @param productKey
     *            the key of review's product
     * @return the stored data of the review
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
    public ProductReview writeReview(ProductReview review, Long productKey)
            throws NonUniqueBusinessKeyException,
            ConcurrentModificationException, OperationNotPermittedException,
            ObjectNotFoundException {

        ArgumentValidator.notNull("review", review);
        ArgumentValidator.notNull("productKey", productKey);

        Product product = loadProduct(productKey.longValue());
        initFeedback(product);

        boolean update = review.getKey() != 0;
        if (update) {
            updatedReview(review);
        } else {
            createReview(review, product);
        }

        product.getProductFeedback().updateAverageRating();
        return review;
    }

    /**
     * Deletes the specified review for a service.
     * <p>
     * Required role: any user role in an organization to delete one's own
     * reviews; administrator of the organization to delete the reviews of other
     * users
     * 
     * @param reviewKey
     *            the key of the review to be deleted
     * @throws OperationNotPermittedException
     *             if the calling user is not the owner of the review or an
     *             administrator of the organization that subscribed to the
     *             service
     * @throws ObjectNotFoundException
     *             if the review is not found
     */
    public void deleteReview(Long reviewKey)
            throws OperationNotPermittedException, ObjectNotFoundException {
        ArgumentValidator.notNull("reviewKey", reviewKey);
        ProductReview review = dm.getReference(ProductReview.class,
                reviewKey.longValue());
        deleteReviewInt(review, false, null);
    }

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
    public void deleteReviewByMarketplaceOwner(Long reviewKey, String reason)
            throws OperationNotPermittedException, ObjectNotFoundException {
        ArgumentValidator.notNull("reviewKey", reviewKey);
        ArgumentValidator.notEmptyString("reason", reason);
        ProductReview review = dm.getReference(ProductReview.class,
                reviewKey.longValue());
        deleteReviewInt(review, true, reason);
    }

    /**
     * Deletes the reviews which the specified user wrote.
     * 
     * @param user
     *            the platform user who wrote the review to delete
     * @param needsPermissionCheck
     *            if the permission check of user to delete review is done or
     *            not
     * @throws OperationNotPermittedException
     *             If the calling user is neither the owner of the review nor an
     *             administrator of the organization that subscribed to the
     *             service. If parameter <code>needsPermissionCheck</code> is
     *             <code>false</code>, this exception never happen.
     */
    public void deleteReviewsOfUser(PlatformUser user,
            boolean needsPermissionCheck) throws OperationNotPermittedException {

        if (user != null) {
            List<ProductReview> reviews = productReviewDao
                    .getProductReviewsForUser(user);
            if (reviews != null && reviews.size() > 0) {
                for (ProductReview reviewToBeRemoved : reviews) {
                    deleteReviewInt(reviewToBeRemoved, false, null,
                            needsPermissionCheck);
                }
            }
        }

    }

    /**
     * Create a new review domain object or retrieve the existing one
     * 
     * @param objectKey
     *            key of ProductReview. If <code>objectKey</code> is
     *            <code>0</code>, create new ProductReview object. If
     *            <code>objectKey</code> is not <code>0</code>, try to find
     *            existing ProductReview object.
     * 
     * @return the stored data of the review
     * @throws ObjectNotFoundException
     *             in case the ProductReview object wasn't found
     */
    public ProductReview createOrFindDomainObject(long objectKey)
            throws ObjectNotFoundException {
        ProductReview review;
        boolean update = objectKey != 0;
        if (update) {
            review = dm.getReference(ProductReview.class, objectKey);
        } else {
            review = new ProductReview();
        }
        return review;
    }

    /**
     * Internal method to delete review comments.
     * 
     * @param review
     *            the review to be deleted.
     * @param mplMarketplaceOwnerDelete
     *            the deletion is triggered by a marketplace owner
     * @param reason
     *            the reason in case of an enforced deletion. Must not be null
     *            an case enforced=true.
     * @throws OperationNotPermittedException
     *             thrown in case the caller is not allowed to perform the
     *             operation.
     * @throws ObjectNotFoundException
     *             thrown in case the review object was not found.
     */
    private void deleteReviewInt(ProductReview review,
            boolean mplMarketplaceOwnerDelete, String reason)
            throws OperationNotPermittedException, ObjectNotFoundException {
        deleteReviewInt(review, mplMarketplaceOwnerDelete, reason, true);
    }

    /**
     * Internal method to delete review comments.
     * 
     * @param reviewToBeRemoved
     *            the review to be deleted.
     * @param mplMarketplaceOwnerDelete
     *            the deletion is triggered by a marketplace owner
     * @param reason
     *            the reason in case of an enforced deletion. Must not be null
     *            an case enforced=true.
     * @param needsPermissionCheck
     *            the deletion need to check permission
     * @throws OperationNotPermittedException
     *             thrown in case the caller is not allowed to perform the
     *             operation.
     */
    private void deleteReviewInt(ProductReview reviewToBeRemoved,
            boolean mplMarketplaceOwnerDelete, String reason,
            boolean needsPermissionCheck) throws OperationNotPermittedException {

        if (needsPermissionCheck) {
            if (!(isAuthor(reviewToBeRemoved) || isAdminOfAuthor(reviewToBeRemoved))
                    && !mplMarketplaceOwnerDelete)
                throw new OperationNotPermittedException(
                        "You must be author or admin for the organization of the reviewer.");

            if (mplMarketplaceOwnerDelete
                    && !(hasMarketplaceOwnerRights(reviewToBeRemoved)))
                throw new OperationNotPermittedException(
                        "You must have marketplace owner role for the organization the review was published.");
        }

        ProductFeedback feedback = reviewToBeRemoved.getProductFeedback();
        feedback.getProductReviews().remove(reviewToBeRemoved);
        dm.remove(reviewToBeRemoved);
        feedback.updateAverageRating();

        if (mplMarketplaceOwnerDelete) {

            String serviceId = localizer.getLocalizedTextFromDatabase(
                    reviewToBeRemoved.getPlatformUser().getLocale(), feedback
                            .getProduct().getKey(),
                    LocalizedObjectTypes.PRODUCT_MARKETING_NAME);

            if (serviceId.equals("")) {
                serviceId = feedback.getProduct().getProductId();
            }
            String[] params = new String[2];
            params[0] = serviceId;
            params[1] = reason;
            try {
                Marketplace marketplace = this
                        .getMarketplaceOwned(reviewToBeRemoved);
                cs.sendMail(reviewToBeRemoved.getPlatformUser(),
                        EmailType.REVIEW_REMOVED_BY_MARKETPLACE_ADMIN, params,
                        marketplace);
            } catch (MailOperationException e) {
                // The mail cannot be send to the creator of the review. Since
                // it is essential to remove the review from the marketplace,
                // the exception will be just logged and to complete the delete
                // function successfully.
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_MAIL_OPERATION_FAILED);
            }
        }
    }

    /**
     * Check if the current user is an administrator of the organization which
     * the creator of the review belongs to
     * 
     * @param reviewToBeRemoved
     *            the review to be deleted.
     * @return whether the current user is an administrator of the organization
     *         which the creator of the review belongs to
     */
    private boolean isAdminOfAuthor(ProductReview reviewToBeRemoved) {

        Organization org = reviewToBeRemoved.getPlatformUser()
                .getOrganization();
        if (getLoggedInUser().isOrganizationAdmin()
                && getLoggedInUser().getOrganization().equals(org)) {
            return true;
        }
        return false;
    }

    /**
     * checks if the currently logged in user is the author of the review to be
     * removed.
     * 
     * @param reviewToBeRemoved
     *            the review to be deleted.
     * @return whether the currently logged in user is the author of the review
     *         to be removed.
     */
    private boolean isAuthor(ProductReview reviewToBeRemoved) {
        if (getLoggedInUser().equals(reviewToBeRemoved.getPlatformUser())) {
            return true;
        } else
            return false;

    }

    /**
     * checks if the logged in user has marketplace owner rights on the review
     * to be deleted.
     * 
     * @param reviewToBeRemoved
     *            the review to be deleted.
     * @return whether the current user has rights to delete review,
     */
    private boolean hasMarketplaceOwnerRights(ProductReview reviewToBeRemoved) {

        if (getMarketplaceOwned(reviewToBeRemoved) != null)
            return true;
        else
            return false;
    }

    /**
     * find the first marketplace that is owned by the logged in user and has
     * published the product of the review to be deleted.
     * 
     * @param reviewToBeRemoved
     *            the review to be deleted.
     * @return the first marketplace that is owned by the logged in user and has
     *         published the product of the review to be deleted.
     */
    private Marketplace getMarketplaceOwned(ProductReview reviewToBeRemoved) {

        final PlatformUser user = getLoggedInUser();
        final Organization org = user.getOrganization();
        Set<Marketplace> maketplaces = reviewToBeRemoved
                .getPublishedMarketplaces();

        for (Marketplace mpl : maketplaces) {
            if (mpl.getOrganization().equals(org)) {
                return mpl;
            }
        }
        return null;
    }

    /**
     * Load product data
     * 
     * @param productKey
     *            key of product object
     * @return the stored data of product object
     * @throws ObjectNotFoundException
     *             if the review is not found
     */
    private Product loadProduct(long productKey) throws ObjectNotFoundException {
        Product product = dm.getReference(Product.class, productKey);
        if (product.isCopy()) {
            return product.getTemplate();
        }
        return product;
    }

    /**
     * initialize feedback information for the product
     * 
     * @param product
     *            product object which the feedback information described for.
     * @throws NonUniqueBusinessKeyException
     *             if the feedback to be created already exists
     */
    private void initFeedback(Product product)
            throws NonUniqueBusinessKeyException {
        if (product.getProductFeedback() == null) {
            ProductFeedback feedback = new ProductFeedback();
            feedback.setAverageRating(new BigDecimal(0));
            feedback.setProduct(product);
            product.setProductFeedback(feedback);
            dm.persist(feedback);
        }
    }

    /**
     * Update new review to the database
     * 
     * @param productReview
     *            review need to be updated
     * @throws ObjectNotFoundException
     *             if the review is not found
     * @throws ConcurrentModificationException
     *             if the review has been modified concurrently
     * @throws OperationNotPermittedException
     *             if the caller is not allowed to perform the operation.
     */
    private void updatedReview(ProductReview productReview)
            throws ObjectNotFoundException, ConcurrentModificationException,
            OperationNotPermittedException {
        checkIfAllowedToModify(productReview);
        try {
            dm.flush();
        } catch (EJBTransactionRolledbackException e) {
            ConcurrentModificationException cme = new ConcurrentModificationException(
                    productReview.getClass().getSimpleName(),
                    productReview.getVersion());
            cme.fillInStackTrace();
            throw cme;
        }
    }

    /**
     * Create new review in database
     * 
     * @param review
     *            review need to be created
     * @param product
     *            product object which the review information described for.
     * @throws OperationNotPermittedException
     *             if the caller is not allowed to perform the operation.
     * @throws NonUniqueBusinessKeyException
     *             if the feedback to be created already exists
     */
    private void createReview(ProductReview review, Product product)
            throws OperationNotPermittedException,
            NonUniqueBusinessKeyException {
        checkIfAllowedToCreate(product);
        review.setPlatformUser(getLoggedInUser());
        review.setProductFeedback(product.getProductFeedback());
        dm.persist(review);
        product.getProductFeedback().getProductReviews().add(review);
    }

    /**
     * Check if the caller is allowed to modify review
     * 
     * @param review
     *            review need to be modified
     * @throws OperationNotPermittedException
     *             if the caller is not allowed to perform the operation.
     */
    private void checkIfAllowedToModify(ProductReview review)
            throws OperationNotPermittedException {
        if (!review.isAllowedToModify(getLoggedInUser())) {
            throw new OperationNotPermittedException(
                    "You must be owner in order to modify.");
        }
    }

    /**
     * Check if the caller is allowed to create review
     * 
     * @param review
     *            review need to be created
     * @throws OperationNotPermittedException
     *             if the caller is not allowed to perform the operation.
     */
    private void checkIfAllowedToCreate(Product product)
            throws OperationNotPermittedException {
        if (!product.isAllowedToCreateReview(getLoggedInUser())) {
            throw new OperationNotPermittedException(
                    "Your organization must be subscribed and you must either have a usage license or be administrator.");
        }
    }

    /**
     * Returns the currently logged in user
     * 
     * @return the currently logged in user
     */
    private PlatformUser getLoggedInUser() {
        PlatformUser currentUser = dm.getCurrentUserIfPresent();
        if (currentUser == null) {
            throw new InvalidUserSession("Login required to create a review.");
        }
        return currentUser;
    }

}
