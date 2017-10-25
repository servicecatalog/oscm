/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Christoph Held                    
 *                                                                              
 *  Creation Date: 30.1.2014                                              
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
public class EnterpriseLandingpageIT extends DomainObjectTestBase {
	Organization platformOperatorOrg;
	List<Product> products;

	private static final int NUMBER_PRODUCTS = 2;

	@Override
	public void setup(final TestContainer container) throws Exception {
		super.setup(container);
		createPlatformOperator();
		createProducts();
	}

	/**
	 * Properly remove all objects from public landing page and create needed
	 * ones for enterprise landing page
	 */
	@Test
	public void switchLandingPage() throws Exception {

		// given marketplace with public landing page
		Marketplace marketplace = runTX(new Callable<Marketplace>() {
			@Override
			public Marketplace call() throws Exception {
				Marketplace marketplace = Marketplaces.createMarketplace(
						platformOperatorOrg, "123456789", true, mgr);
				Marketplaces.createLandingpageProducts(
						marketplace.getPublicLandingpage(), products, mgr);
				return marketplace;
			}
		});

		// when switching to enterprise landing page
		switchLandingpage(marketplace.getMarketplaceId());

		// then make sure enterprise landing page is properly saved
		marketplace = reload(marketplace);
		assertNotNull(marketplace.getEnterpriseLandingpage());
		assertNull(marketplace.getPublicLandingpage());

	}

	private Void switchLandingpage(final String marketplaceId) throws Exception {
		return runTX(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Marketplace marketplace = (Marketplace) mgr
						.find(new Marketplace(marketplaceId));

				// remove old landing page
				marketplace.setPublicLandingpage(null);
				mgr.remove(marketplace.getPublicLandingpage());

				// create new landing page
				EnterpriseLandingpage newLandingPage = new EnterpriseLandingpage();
				newLandingPage.setMarketplace(marketplace);
				mgr.persist(newLandingPage);
				marketplace.setEnterpiseLandingpage(newLandingPage);
				return null;
			}
		});
	}

	private void createPlatformOperator() throws Exception {
		if (platformOperatorOrg == null) {
			runTX(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					platformOperatorOrg = Organizations.createOrganization(mgr,
							OrganizationRoleType.PLATFORM_OPERATOR);

					PlatformUser platformOperatorUser = Organizations
							.createUserForOrg(mgr, platformOperatorOrg, true,
									"Administrator");
					PlatformUsers.grantRoles(mgr, platformOperatorUser,
							UserRoleType.PLATFORM_OPERATOR);

					return null;
				}
			});
		}
	}

	private void createProducts() throws Exception {
		runTX(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				products = Products.createTestData(mgr, platformOperatorOrg,
						NUMBER_PRODUCTS);
				return null;
			}
		});
		assertEquals(NUMBER_PRODUCTS, products.size());
	}
}
