/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: brandstetter                                                      
 *                                                                              
 *  Creation Date: 31.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;

public class SharesDataRetrievalServiceBeanIT extends EJBTestBase {

	private static final String ANY_MARKETPLACE_ID = "marketplaceId";
	private static final long ANY_MARKETPLACE_KEY = 0;
	private long MARKETPLACE_KEY;
	private DataService ds;
	private SharesDataRetrievalServiceLocal calculator;
	private SharesDataRetrievalServiceBean sharesRetrievalServiceBean;

	private Organization platformOperatorOrg;
	private static PlatformUser platformOperatorUser;
	private Organization supplierOrg;
	private Marketplace supplierMp;

	private TechnicalProduct technicalProduct;
	private Product product2;
	private String productId1 = "productId_1";
	private String productId2 = "productId_2";
	private Long catalogEntryKey1;
	private Long catalogEntryKey2;

	private long beforeSetupTime = 0;
	private long setupTime = 0;
	private long creationTime = 0;
	private long updateTime1 = 0;
	private long updateTime2 = 0;
	private static final int TEST_EXECUTION_TIME = 1000 * 10;

	/**
	 * MP----CatalogEntry1----Product1(templ)----Product1(sub)----Subscription1 <br>
	 * | \....................| <br>
	 * RS \...................PM1 <br>
	 * ....\ <br>
	 * ....CatalogEntry2----Product2(resale)----Product2(sub)----Subscription2 <br>
	 * ........|............| <br>
	 * ........PM2..........PM1 <br>
	 */
	public void setup(final TestContainer container) throws Exception {
		container.enableInterfaceMocking(true);
		container.login("1");
		container.addBean(new DataServiceBean());
		container.addBean(new SharesDataRetrievalServiceBean());
		ds = container.get(DataService.class);
		calculator = container.get(SharesDataRetrievalServiceLocal.class);
		sharesRetrievalServiceBean = new SharesDataRetrievalServiceBean();

		// ------------------------ setup db ------------------------
		// In order to avoid fiddling with dates in productive code (like
		// creation-time), we simple use the current time and add and subtract
		// something

		setupTime = System.currentTimeMillis();// start creating objects
		beforeSetupTime = setupTime - TEST_EXECUTION_TIME; // before the first
															// object is
		// created
		creationTime = setupTime + 1000;// all objects should be created
		updateTime1 = setupTime + 10 * 1000;// first object update (to get
											// history objects)
		updateTime2 = setupTime + 20 * 1000;// second object update (to get
											// history objects)

		// organizations
		createPlatformOperatorOrganization();
		createSupplierOrganization();

		// marketplace + RevenueShareModel
		createSupplierMarketplace();

		// products + catalog entries
		createTechnicalProduct();
		catalogEntryKey1 = createAndPublishProduct(productId1,
				"priceModelId_1", true, setupTime);
		catalogEntryKey2 = createAndPublishProduct(productId2,
				"priceModelId_2", true, setupTime);

		// RevenueShareModel for CatalogEntries
		createCatalogEntryRevenueShares(catalogEntryKey1, 30, setupTime);
		createCatalogEntryRevenueShares(catalogEntryKey2, 60, setupTime);

		// update RevenueShareModelHistory entries for Marketplace:
		// ...........................Broker,Reseller,Supplier
		// 1) modDate: setup time.....value 1,2,3
		// 2) modDate: update time1...value 11,12,13
		// 3) modDate: update time2...value 21,22,23
		updateMarketplaceRevenueShares(10, updateTime1);
		updateMarketplaceRevenueShares(20, updateTime2);

		// update RevenueShareModelHistory entries for CatalogEntry1:
		// ...........................Broker,Reseller
		// 1) modDate: setup time.....value 31,32
		// 2) modDate: update time1...value 41,42
		// 3) modDate: update time2...value 51,52
		updateCatalogEntryRevenueShares(catalogEntryKey1, 40, updateTime1);
		updateCatalogEntryRevenueShares(catalogEntryKey1, 50, updateTime2);

		// update RevenueShareModelHistory entries for CatalogEntry2:
		// ...........................Broker,Reseller
		// 1) modDate: setup time.....value 61,62
		// 2) modDate: update time1...value 71,72
		// 3) modDate: update time2...value 81,82
		updateCatalogEntryRevenueShares(catalogEntryKey2, 70, updateTime1);
		updateCatalogEntryRevenueShares(catalogEntryKey2, 80, updateTime2);

		// update CatalogEntries
		updateCatalogEntry(catalogEntryKey1, updateTime1);
		updateCatalogEntry(catalogEntryKey2, updateTime1);

	}

	private void createPlatformOperatorOrganization() throws Exception {
		if (platformOperatorOrg == null) {
			runTX(new Callable<Void>() {
				public Void call() throws Exception {
					platformOperatorOrg = Organizations.createOrganization(ds,
							OrganizationRoleType.PLATFORM_OPERATOR);

					platformOperatorUser = Organizations.createUserForOrg(ds,
							platformOperatorOrg, true, "Administrator");
					PlatformUsers.grantRoles(ds, platformOperatorUser,
							UserRoleType.PLATFORM_OPERATOR);
					return null;
				}
			});
		}
	}

	private void createSupplierOrganization() throws Exception {
		runTX(new Callable<Void>() {
			public Void call() throws Exception {
				supplierOrg = Organizations.createOrganization(ds,
						OrganizationRoleType.MARKETPLACE_OWNER,
						OrganizationRoleType.PLATFORM_OPERATOR,
						OrganizationRoleType.TECHNOLOGY_PROVIDER);

				PlatformUser user = Organizations.createUserForOrg(ds,
						supplierOrg, true, "admin");
				PlatformUsers.grantRoles(ds, user,
						UserRoleType.PLATFORM_OPERATOR);
				PlatformUsers.grantRoles(ds, user,
						UserRoleType.MARKETPLACE_OWNER);
				return null;
			}
		});
	}

	private void createSupplierMarketplace() throws Exception {
		runTX(new Callable<Void>() {
			public Void call() throws Exception {
				supplierMp = Marketplaces.createMarketplace(supplierOrg,
						ANY_MARKETPLACE_ID, false, ds);
				MARKETPLACE_KEY = supplierMp.getKey();
				return null;
			}
		});
	}

	private TechnicalProduct createTechnicalProduct() throws Exception {
		return runTX(new Callable<TechnicalProduct>() {
			public TechnicalProduct call() throws Exception {
				technicalProduct = TechnicalProducts.createTechnicalProduct(ds,
						supplierOrg, "tp", false, ServiceAccessType.LOGIN);
				return technicalProduct;
			}
		});
	}

	private Long createAndPublishProduct(final String productId,
			final String priceModelId, final boolean published,
			final long historyModificationTime) throws Exception {
		Long catalogEntryKey = runTX(new Callable<Long>() {
			public Long call() throws Exception {
				Product product = Products.createProduct(supplierOrg,
						technicalProduct, true, productId, priceModelId, ds);
				product.getPriceModel().getKey();
				product.setStatus(ServiceStatus.ACTIVE);
				product.setHistoryModificationTime(Long
						.valueOf(historyModificationTime));
				ds.persist(product);
				if (productId.equals("productId_2")) {
					product2 = product;
				}

				Long ceKey = null;
				if (published) {
					CatalogEntry catalogEntry = new CatalogEntry();
					catalogEntry.setProduct(product);
					catalogEntry.setMarketplace(supplierMp);
					catalogEntry.setVisibleInCatalog(true);
					catalogEntry.setAnonymousVisible(true);
					catalogEntry.setHistoryModificationTime(Long
							.valueOf(historyModificationTime));
					ds.persist(catalogEntry);
					ceKey = Long.valueOf(catalogEntry.getKey());
				}
				return ceKey;
			}
		});
		return catalogEntryKey;
	}

	private void updateMarketplaceRevenueShares(final int revenueShareBase,
			final long historyModificationTime) throws Exception {
		runTX(new Callable<Void>() {
			public Void call() throws Exception {
				Marketplace marketplace = Marketplaces.findMarketplace(ds,
						ANY_MARKETPLACE_ID);

				RevenueShareModel brokerPriceModel = marketplace
						.getBrokerPriceModel();
				brokerPriceModel.setRevenueShare(new BigDecimal(
						revenueShareBase + 1));
				brokerPriceModel.setHistoryModificationTime(Long
						.valueOf(historyModificationTime));
				ds.persist(brokerPriceModel);

				RevenueShareModel resellerPriceModel = marketplace
						.getResellerPriceModel();
				resellerPriceModel.setRevenueShare(new BigDecimal(
						revenueShareBase + 2));
				resellerPriceModel.setHistoryModificationTime(Long
						.valueOf(historyModificationTime));
				ds.persist(resellerPriceModel);

				RevenueShareModel priceModel = marketplace.getPriceModel();
				priceModel
						.setRevenueShare(new BigDecimal(revenueShareBase + 3));
				priceModel.setHistoryModificationTime(Long
						.valueOf(historyModificationTime));
				ds.persist(priceModel);
				return null;
			}
		});
	}

	private void createCatalogEntryRevenueShares(final Long catalogEntryKey,
			final long revenueShareBase, final long historyModificationTime)
			throws Exception {
		runTX(new Callable<Void>() {
			public Void call() throws Exception {
				CatalogEntry catalogEntry = ds.find(CatalogEntry.class,
						catalogEntryKey);

				RevenueShareModel brokerPriceModel = new RevenueShareModel();
				brokerPriceModel
						.setRevenueShareModelType(RevenueShareModelType.BROKER_REVENUE_SHARE);
				brokerPriceModel.setRevenueShare(new BigDecimal(
						revenueShareBase + 1));
				brokerPriceModel.setHistoryModificationTime(Long
						.valueOf(historyModificationTime));
				catalogEntry.setBrokerPriceModel(brokerPriceModel);
				ds.persist(brokerPriceModel);

				RevenueShareModel resellerPriceModel = new RevenueShareModel();
				resellerPriceModel
						.setRevenueShareModelType(RevenueShareModelType.RESELLER_REVENUE_SHARE);
				resellerPriceModel.setRevenueShare(new BigDecimal(
						revenueShareBase + 2));
				resellerPriceModel.setHistoryModificationTime(Long
						.valueOf(historyModificationTime));
				catalogEntry.setResellerPriceModel(resellerPriceModel);
				ds.persist(resellerPriceModel);
				return null;
			}
		});
	}

	private void updateCatalogEntryRevenueShares(final Long catalogEntryKey,
			final int revenueShareBase, final long historyModificationTime)
			throws Exception {
		runTX(new Callable<Void>() {
			public Void call() throws Exception {

				CatalogEntry catalogEntry = ds.find(CatalogEntry.class,
						catalogEntryKey);

				RevenueShareModel brokerPriceModel = catalogEntry
						.getBrokerPriceModel();
				brokerPriceModel.setRevenueShare(new BigDecimal(
						revenueShareBase + 1));
				brokerPriceModel.setHistoryModificationTime(Long
						.valueOf(historyModificationTime));
				ds.persist(brokerPriceModel);

				RevenueShareModel resellerPriceModel = catalogEntry
						.getResellerPriceModel();
				resellerPriceModel.setRevenueShare(new BigDecimal(
						revenueShareBase + 2));
				resellerPriceModel.setHistoryModificationTime(Long
						.valueOf(historyModificationTime));
				ds.persist(resellerPriceModel);
				return null;
			}
		});
	}

	private void updateCatalogEntry(final Long catalogEntryKey,
			final long historyModificationTime) throws Exception {
		runTX(new Callable<Void>() {
			public Void call() throws Exception {
				CatalogEntry catalogEntry = ds.find(CatalogEntry.class,
						catalogEntryKey);
				catalogEntry.setAnonymousVisible(false);
				catalogEntry.setHistoryModificationTime(Long
						.valueOf(historyModificationTime));
				ds.persist(catalogEntry);
				return null;
			}
		});
	}

	/***************************************************************************/
	/********************************* TESTS ***********************************/
	/***************************************************************************/

	@Test(expected = IllegalArgumentException.class)
	public void loadMarketplaceRevenueSharePercentage_billingPeriodEndIsZero()
			throws Exception {
		// when
		sharesRetrievalServiceBean.loadMarketplaceRevenueSharePercentage(
				ANY_MARKETPLACE_KEY, 0);
	}

	/**
	 * Load revenue share for marketplace owner with a given period that ends
	 * before the history data are created.
	 */
	@Test
	public void loadMarketplaceRevenueSharePercentage_mp_beforeCreationTime()
			throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadMarketplaceRevenueSharePercentage(
						MARKETPLACE_KEY, beforeSetupTime);
			}
		});

		// then
		assertNull(revenueShare);
	}

	/**
	 * Load revenue share for marketplace owner with a given period that ends
	 * after the history data are created.
	 */
	@Test
	public void loadMarketplaceRevenueSharePercentage_mp_creationTime()
			throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadMarketplaceRevenueSharePercentage(
						MARKETPLACE_KEY, creationTime);
			}
		});

		// then
		assertNotNull(revenueShare);
		assertEquals(0, revenueShare.compareTo(new BigDecimal(0)));
	}

	/**
	 * Load revenue share for marketplace owner with a given period that ends
	 * after the history data are updated the first time (new history entry).
	 */
	@Test
	public void loadMarketplaceRevenueSharePercentage_mp_updateTime1()
			throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadMarketplaceRevenueSharePercentage(
						MARKETPLACE_KEY, updateTime1);
			}
		});

		// then
		assertNotNull(revenueShare);
		assertEquals(new BigDecimal(13), revenueShare.stripTrailingZeros());
	}

	/**
	 * Load revenue share for marketplace owner with a given period that ends
	 * after the history data are updated the second time (new history entry).
	 */
	@Test
	public void loadMarketplaceRevenueSharePercentage_mp_updateTime2()
			throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadMarketplaceRevenueSharePercentage(
						MARKETPLACE_KEY, updateTime2);
			}
		});

		// then
		assertNotNull(revenueShare);
		assertEquals(new BigDecimal(23), revenueShare.stripTrailingZeros());
	}

	@Test(expected = java.lang.NullPointerException.class)
	public void loadRevenueSharePercentageForSeller_billingPeriodEndIsZero()
			throws Exception {
		// when
		sharesRetrievalServiceBean.loadRevenueSharePercentageForSeller(
				product2.getKey(), "Broker", 0);
	}

	@Test
	public void loadBrokerRevenueSharePercentage_unkownProductKey()
			throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadBrokerRevenueSharePercentage(10,
						setupTime);
			}
		});

		// then
		assertNull(revenueShare);
	}

	/**
	 * Load revenue share for broker with a given period that ends before the
	 * history data are created.
	 */
	@Test
	public void loadBrokerRevenueSharePercentage_beforeCreationTime()
			throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadBrokerRevenueSharePercentage(
						product2.getKey(), beforeSetupTime);
			}
		});

		// then
		assertNull(revenueShare);
	}

	/**
	 * Load revenue share for broker with a given period that ends after the
	 * history data are created.
	 */
	@Test
	public void loadBrokerRevenueSharePercentage_creationTime()
			throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadBrokerRevenueSharePercentage(
						product2.getKey(), creationTime);
			}
		});

		// then
		assertNotNull(revenueShare);
		assertEquals(new BigDecimal(61), revenueShare.stripTrailingZeros());
	}

	/**
	 * Load revenue share for broker with a given period that ends after the
	 * history data are updated the first time (new history entry).
	 */
	@Test
	public void loadBrokerRevenueSharePercentage_updateTime1() throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadBrokerRevenueSharePercentage(
						product2.getKey(), updateTime1);
			}
		});

		// then
		assertNotNull(revenueShare);
		assertEquals(new BigDecimal(71), revenueShare.stripTrailingZeros());
	}

	/**
	 * Load revenue share for broker with a given period that ends after the
	 * history data are updated the second time (new history entry).
	 */
	@Test
	public void loadBrokerRevenueSharePercentage_updateTime2() throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadBrokerRevenueSharePercentage(
						product2.getKey(), updateTime2);
			}
		});

		// then
		assertNotNull(revenueShare);
		assertEquals(new BigDecimal(81), revenueShare.stripTrailingZeros());
	}

	/**
	 * Load revenue share for reseller with a given period that ends before the
	 * history data are created.
	 */
	@Test
	public void loadResellerRevenueSharePercentage_beforeCreationTime()
			throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadResellerRevenueSharePercentage(
						product2.getKey(), beforeSetupTime);
			}
		});

		// then
		assertNull(revenueShare);
	}

	/**
	 * Load revenue share for reseller with a given period that ends after the
	 * history data are created.
	 */
	@Test
	public void loadResellerRevenueSharePercentage_creationTime()
			throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadResellerRevenueSharePercentage(
						product2.getKey(), creationTime);
			}
		});

		// then
		assertNotNull(revenueShare);
		assertEquals(new BigDecimal(62), revenueShare.stripTrailingZeros());
	}

	/**
	 * Load revenue share for reseller with a given period that ends after the
	 * history data are updated the first time (new history entry).
	 */
	@Test
	public void loadResellerRevenueSharePercentage_updateTime1()
			throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadResellerRevenueSharePercentage(
						product2.getKey(), updateTime1);
			}
		});

		// then
		assertNotNull(revenueShare);
		assertEquals(new BigDecimal(72), revenueShare.stripTrailingZeros());
	}

	/**
	 * Load revenue share for reseller with a given period that ends after the
	 * history data are updated the second time (new history entry).
	 */
	@Test
	public void loadResellerRevenueSharePercentage_updateTime2()
			throws Exception {
		// given see setup

		// when
		BigDecimal revenueShare = runTX(new Callable<BigDecimal>() {
			public BigDecimal call() throws Exception {
				return calculator.loadResellerRevenueSharePercentage(
						product2.getKey(), updateTime2);
			}
		});

		// then
		assertNotNull(revenueShare);
		assertEquals(new BigDecimal(82), revenueShare.stripTrailingZeros());
	}
}
