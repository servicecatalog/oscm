/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-26                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reviewservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJBTransactionRolledbackException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductFeedback;
import org.oscm.domobjects.ProductReview;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.reviewservice.dao.ProductReviewDao;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.exceptions.InvalidUserSession;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.OperationNotPermittedException;

/**
 * Unit test of ReviewServiceLocalBean
 * 
 * @author Gao
 * 
 */
public class ReviewServiceLocalBeanTest {
    private ReviewServiceLocalBean reviewBean;
    private DataService dm;
    private ProductReviewDao productReviewDao;
    private CommunicationServiceLocal cs;
    private LocalizerServiceLocal localizer;
    private PlatformUser currentUser;
    private Product product;
    private ProductReview productReview;
    private Organization org;
    private ProductFeedback feedback;
    private Long reviewKey;

    @Before
    public void setup() throws Exception {
        product = spy(new Product());
        productReview = new ProductReview();
        feedback = new ProductFeedback();
        reviewBean = new ReviewServiceLocalBean();
        dm = mock(DataService.class);
        productReviewDao = mock(ProductReviewDao.class);
        cs = mock(CommunicationServiceLocal.class);
        localizer = mock(LocalizerServiceLocal.class);
        reviewBean.dm = dm;
        reviewBean.cs = cs;
        reviewBean.productReviewDao = productReviewDao;
        reviewBean.localizer = localizer;
        currentUser = new PlatformUser();
        currentUser.setLocale("en");
        org = new Organization();
        currentUser.setOrganization(org);
        reviewKey = Long.valueOf(1);
        doReturn(productReview).when(dm).getReference(ProductReview.class,
                reviewKey.longValue());
        doReturn(currentUser).when(dm).getCurrentUserIfPresent();
        doReturn("").when(localizer).getLocalizedTextFromDatabase(eq("en"),
                anyLong(), eq(LocalizedObjectTypes.PRODUCT_MARKETING_NAME));
    }

    @Test
    public void writeReview_Create() throws Exception {
        // given
        initProduct(1l);
        initProductReview(0l);
        doReturn(product).when(dm).getReference(Product.class, 1l);

        // when
        ProductReview result = reviewBean.writeReview(productReview,
                Long.valueOf(product.getKey()));
        // then
        verify(dm, times(1)).persist(eq(productReview));
        assertEquals(result.getKey(), productReview.getKey());
    }

    @Test(expected = InvalidUserSession.class)
    public void writeReview_CreateNotAllowed_InvalidUserSession()
            throws Exception {
        // given
        doReturn(null).when(dm).getCurrentUserIfPresent();
        initProduct(1l);
        initProductReview(0l);
        doReturn(product).when(dm).getReference(Product.class, 1l);
        // when
        try {
            reviewBean.writeReview(productReview,
                    Long.valueOf(product.getKey()));
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void writeReview_CreateNotAllowed_OperationNotPermittedException()
            throws Exception {
        // given
        initProduct(1l, ServiceAccessType.DIRECT);
        initProductReview(0l);
        doReturn(product).when(dm).getReference(Product.class, 1l);
        // when
        try {
            reviewBean.writeReview(productReview,
                    Long.valueOf(product.getKey()));
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void writeReview_ProductIsCopy() throws Exception {
        // given
        initProduct(1l);
        initProductReview(2l);
        productReview.setPlatformUser(currentUser);
        Product copyProduct = new Product();
        copyProduct.setTemplate(product);
        copyProduct.setKey(3l);
        doReturn(product).when(dm).getReference(Product.class, 1l);
        doReturn(copyProduct).when(dm).getReference(Product.class, 3l);

        // when
        ProductReview result = reviewBean.writeReview(productReview,
                Long.valueOf(copyProduct.getKey()));
        // then
        verify(dm, times(1)).flush();
        assertEquals(result.getKey(), productReview.getKey());
    }

    @Test
    public void writeReview_Update() throws Exception {
        // given
        initProduct(1l);
        initProductReview(2l);
        doReturn(product).when(dm).getReference(Product.class, 1l);
        productReview.setPlatformUser(currentUser);

        // when
        ProductReview result = reviewBean.writeReview(productReview,
                Long.valueOf(product.getKey()));
        // then
        verify(dm, times(1)).flush();
        assertEquals(result.getKey(), productReview.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeReview_ReviewIsNull() throws Exception {
        // given
        initProduct(1l);
        // when
        try {
            reviewBean.writeReview(null, Long.valueOf(product.getKey()));
            // then
            fail();
        } catch (Exception e) {
            assertEquals(Boolean.TRUE, Boolean.valueOf(e.getMessage().contains(
                    "must not be null")));
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeReview_ProductKeyIsNull() throws Exception {
        // given
        initProductReview(2l);
        // when
        try {
            reviewBean.writeReview(productReview, null);
            // then
            fail();
        } catch (Exception e) {
            assertEquals(Boolean.TRUE, Boolean.valueOf(e.getMessage().contains(
                    "must not be null")));
            throw e;
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void writeReview_OperationNotPermittedException() throws Exception {
        // given
        initProduct(1l);
        initProductReview(2l);
        doReturn(product).when(dm).getReference(Product.class, 1l);
        // when
        try {
            reviewBean.writeReview(productReview,
                    Long.valueOf(product.getKey()));
            // then
            fail();
        } catch (Exception e) {
            assertEquals(
                    Boolean.TRUE,
                    Boolean.valueOf(e.getMessage().contains(
                            "You must be owner in order to modify.")));
            throw e;
        }
    }

    @Test(expected = ConcurrentModificationException.class)
    public void writeReview_ConcurrentModificationException() throws Exception {
        // given
        initProduct(1l);
        initProductReview(2l);
        productReview.setPlatformUser(currentUser);
        doReturn(product).when(dm).getReference(Product.class, 1l);
        doThrow(new EJBTransactionRolledbackException()).when(dm).flush();
        // when
        try {
            reviewBean.writeReview(productReview,
                    Long.valueOf(product.getKey()));
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteReview_IllegalArgumentException() throws Exception {
        // given
        // when
        try {
            reviewBean.deleteReview(null);
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void deleteReview_OperationNotPermittedException() throws Exception {
        // given
        initProductReview(2l);
        productReview.setPlatformUser(new PlatformUser());
        // when
        try {
            reviewBean.deleteReview(reviewKey);
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void deleteReview_OK() throws Exception {
        // given
        initProductReview(2l);
        productReview.setPlatformUser(currentUser);
        // when
        reviewBean.deleteReview(reviewKey);
        // then
        verify(dm, times(1)).remove(eq(productReview));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteReviewByMarketplaceOwner_ReviewIsNull() throws Exception {
        // given
        // when
        try {
            reviewBean.deleteReviewByMarketplaceOwner(null, "");
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteReviewByMarketplaceOwner_ReasonIsNull() throws Exception {
        // given
        initProductReview(2l);
        // when
        try {
            reviewBean.deleteReviewByMarketplaceOwner(reviewKey, null);
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void deleteReviewByMarketplaceOwner_OperationNotPermittedException()
            throws Exception {
        // given
        initProductReview(2l);
        productReview.setPlatformUser(currentUser);
        // when
        try {
            reviewBean.deleteReviewByMarketplaceOwner(reviewKey, "Reason");
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void deleteReviewByMarketplaceOwner_MailOperationException()
            throws Exception {
        // given
        initProductReview(2l);
        addMarketplaceToReview();
        productReview.setPlatformUser(currentUser);
        doThrow(new MailOperationException()).when(cs).sendMail(
                eq(currentUser),
                eq(EmailType.REVIEW_REMOVED_BY_MARKETPLACE_ADMIN),
                any(String[].class), any(Marketplace.class));
        // when
        try {
            reviewBean.deleteReviewByMarketplaceOwner(reviewKey, "Reason");
            // then
        } catch (Exception e) {
            fail();
            throw e;
        }
    }

    @Test
    public void deleteReviewByMarketplaceOwner_ok() throws Exception {
        // given
        initProductReview(2l);
        addMarketplaceToReview();
        productReview.setPlatformUser(currentUser);
        // when
        try {
            reviewBean.deleteReviewByMarketplaceOwner(reviewKey, "Reason");
            // then
            verify(cs, times(1)).sendMail(eq(currentUser),
                    eq(EmailType.REVIEW_REMOVED_BY_MARKETPLACE_ADMIN),
                    any(String[].class), any(Marketplace.class));
        } catch (Exception e) {
            fail();
            throw e;
        }
    }

    @Test
    public void deleteReviewsOfUser_OK() throws Exception {
        // given
        initProductReview(2l);
        addMarketplaceToReview();
        productReview.setPlatformUser(currentUser);
        doReturn(Arrays.asList(productReview)).when(productReviewDao)
                .getProductReviewsForUser(any(PlatformUser.class));
        // when
        try {
            reviewBean.deleteReviewsOfUser(currentUser, false);
            // then
            verify(dm, times(1)).remove(eq(productReview));
        } catch (Exception e) {
            fail();
            throw e;
        }
    }

    @Test
    public void createOrFindDomainObject_Create() throws Exception {
        // given
        // when
        try {
            ProductReview result = reviewBean.createOrFindDomainObject(0l);
            // then
            assertEquals(0, result.getKey());
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void createOrFindDomainObject_Find() throws Exception {
        // given
        initProductReview(1l);
        doReturn(productReview).when(dm).getReference(eq(ProductReview.class),
                anyLong());
        // when
        try {
            ProductReview result = reviewBean.createOrFindDomainObject(1l);
            // then
            assertEquals(1, result.getKey());
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * to mock ProductReview.getPublishedMarketplaces method
     */
    private void addMarketplaceToReview() {
        Marketplace mp = new Marketplace();
        mp.setOrganization(org);
        Set<Marketplace> mps = new HashSet<Marketplace>();
        mps.add(mp);
        CatalogEntry catalogEntry = new CatalogEntry();
        catalogEntry.setMarketplace(mp);
        List<CatalogEntry> catalogEntries = new ArrayList<CatalogEntry>();
        catalogEntries.add(catalogEntry);
        productReview.getProductFeedback().getProduct()
                .setCatalogEntries(catalogEntries);
    }

    /**
     * initialize product object
     * 
     * @param key
     *            tKey of product, 0 means create new DO
     * @param type
     *            ServiceAccessType of TechnicalProduct
     */
    private void initProduct(long key, ServiceAccessType type) {
        product.setKey(key);
        product.setProductId("ProductId");
        product.setTemplate(product);
        TechnicalProduct tp = new TechnicalProduct();
        tp.setAccessType(type);
        product.setTechnicalProduct(tp);
    }

    /**
     * initialize product object
     * 
     * @param key
     *            tKey of product, 0 means create new DO
     */
    private void initProduct(long key) {
        this.initProduct(key, ServiceAccessType.EXTERNAL);
    }

    /**
     * initialize ProductReview object
     * 
     * @param key
     *            tKey of ProductReview, 0 means create new DO
     */
    private void initProductReview(long key) {
        productReview.setKey(key);
        feedback.getProductReviews().add(productReview);
        feedback.setProduct(product);
        productReview.setProductFeedback(feedback);
    }

}
