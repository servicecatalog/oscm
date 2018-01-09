/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Enes Sejfi                      
 *                                                                              
 *  Creation Date: 11.06.2012                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * Unit tests for the Landingpage management.
 */
public class PublicLandingpageIT extends DomainObjectTestBase {
    private Organization platformOperatorOrg;
    private List<Product> products;

    private long platformOperatorUserKey;

    private static final int NUMBER_PRODUCTS = 2;

    @Override
    public void setup(final TestContainer container) throws Exception {
        super.setup(container);
        createPlatformOperator();
        createProducts();
    }

    @Test
    public void deleteMarketplaceWithLandingPage() throws Exception {
        // given
        final Marketplace marketplace = runTX(new Callable<Marketplace>() {
            public Marketplace call() throws Exception {
                Marketplace marketplace = Marketplaces.createMarketplace(
                        platformOperatorOrg, "123456789", true, mgr);
                Marketplaces.createLandingpageProducts(
                        marketplace.getPublicLandingpage(), products, mgr);
                return marketplace;
            }
        });

        assertNotNull(marketplace.getPublicLandingpage());

        // when
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        deleteMarketplace(marketplace.getMarketplaceId());

        // then
        // cascade rule force deletion of landingpage and landingpage products
        assertNull(reloadLandingpage(marketplace));
    }

    private PublicLandingpage reloadLandingpage(final Marketplace marketplace)
            throws Exception {
        return runTX(new Callable<PublicLandingpage>() {
            public PublicLandingpage call() throws Exception {
                return mgr.find(PublicLandingpage.class, marketplace.getPublicLandingpage()
                        .getKey());
            }
        });
    }

    private Void deleteMarketplace(final String marketplaceId) throws Exception {
        return runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Marketplace marketplace = (Marketplace) mgr
                        .find(new Marketplace(marketplaceId));
                mgr.remove(marketplace);
                return null;
            }
        });
    }

    private void createPlatformOperator() throws Exception {
        if (platformOperatorOrg == null) {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    platformOperatorOrg = Organizations.createOrganization(mgr,
                            OrganizationRoleType.PLATFORM_OPERATOR);

                    PlatformUser platformOperatorUser = Organizations
                            .createUserForOrg(mgr, platformOperatorOrg, true,
                                    "Administrator");
                    PlatformUsers.grantRoles(mgr, platformOperatorUser,
                            UserRoleType.PLATFORM_OPERATOR);
                    platformOperatorUserKey = platformOperatorUser.getKey();

                    return null;
                }
            });
        }
    }

    private void createProducts() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                products = Products.createTestData(mgr, platformOperatorOrg,
                        NUMBER_PRODUCTS);
                return null;
            }
        });
        assertEquals(NUMBER_PRODUCTS, products.size());
    }
}
