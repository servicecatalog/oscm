/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: barzu                                                    
 *                                                                              
 *  Creation Date: Oct 14, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 14, 2011                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.oscm.interceptor.DateFactory;
import org.oscm.operationslog.UserOperationLogQuery;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PaymentInfos;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;

/**
 * Provides test data setup for testing the user operations log queries
 * 
 * @author barzu
 */
abstract class UserOperationLogQueryTestBase extends DomainObjectTestBase {

	static final String ORG_NAME = "TheInitialName";
	static final String PRODUCT_ID = "TheInitialProductId";
	static final String PAYMENT_INFO_ID = "TheInitialPaymentInfoId";
	static final String BILLING_CONTACT_ID = "TheInitialBillingContactId";
	static final String USER_FIRST_NAME = "Sir";
	static final String SUBSCRIPTION_ID = "TheInitialSubscriptionId";
	static final String ROLE_DEFINITION_ID = "PRODUCT_BOSS";

	Organization supplier;
	PlatformUser user;
	Product product;
	Marketplace marketplace;
	PaymentType paymentType;
	PaymentInfo paymentInfo;
	BillingContact billingContact;
	Subscription subscription;
	UsageLicense usageLicense;
	RoleDefinition roleDefinition;

	@Override
	public void dataSetup() throws Exception {
		supplier = null;
		user = null;
		product = null;
		marketplace = null;
		paymentType = null;
		paymentInfo = null;
		billingContact = null;
		subscription = null;
		usageLicense = null;
		roleDefinition = null;
	}

	void addOrganization() throws Exception {
		supplier = Organizations.createOrganization(mgr,
				OrganizationRoleType.TECHNOLOGY_PROVIDER,
				OrganizationRoleType.SUPPLIER);
		supplier.setName(ORG_NAME);
	}

	void addAndLoginAsUser() throws Exception {
		addAndLoginAsUser("admin");
	}

	void addAndLoginAsUser(String userId) throws Exception {
		addUser(userId);
		container.login(user.getKey());
	}

	void addUser() throws Exception {
		addUser("admin");
	}

	void addUser(String userId) throws Exception {
		user = Organizations.createUserForOrg(mgr, supplier, true, userId);
		mgr.flush();
		user.setAdditionalName("Bombastic");
		user.setFirstName(USER_FIRST_NAME);
		mgr.flush();
	}

	void addProduct() throws Exception {
		product = Products.createProduct(supplier.getOrganizationId(), "prod",
				"techProd", mgr);
		mgr.flush();
		product.setProductId(PRODUCT_ID);
		mgr.flush();
	}

	void addChargeableProduct() throws Exception {
		addChargeableProduct("prod");
	}

	void addChargeableProduct(String productId) throws Exception {
		TechnicalProduct technicalProduct = TechnicalProducts
				.findTechnicalProduct(mgr, supplier, "techProd");
		if (technicalProduct == null) {
			technicalProduct = TechnicalProducts.createTechnicalProduct(mgr,
					supplier, "techProd", false, ServiceAccessType.LOGIN);
		}
		product = Products.createProduct(supplier, technicalProduct, true,
				productId, "priceModel", null, mgr);
		mgr.flush();
		product.getPriceModel().setPricePerPeriod(BigDecimal.valueOf(29));
		mgr.flush();
	}

	void addMarketplace() throws Exception {
		marketplace = Marketplaces.createGlobalMarketplace(supplier,
				"marketplace", mgr);
		mgr.flush();
		marketplace.setMarketplaceId(GLOBAL_MARKETPLACE_NAME);
		mgr.flush();
	}

	void addPaymentInfo() throws Exception {
		paymentInfo = new PaymentInfo();
		paymentInfo.setPaymentInfoId("paymentInfoId");
		paymentInfo.setOrganization(supplier);
		paymentType = new PaymentType();
		paymentType.setPaymentTypeId(PaymentType.INVOICE);
		paymentType = (PaymentType) mgr.find(paymentType);
		paymentInfo.setPaymentType(paymentType);
		mgr.persist(paymentInfo);
		mgr.flush();
		paymentInfo.setPaymentInfoId(PAYMENT_INFO_ID);
		mgr.flush();
	}

	void addBillingContact() throws Exception {
		billingContact = PaymentInfos.createBillingContact(mgr, supplier);
		billingContact.setBillingContactId(BILLING_CONTACT_ID);
		mgr.flush();
	}

	void addSubscription() throws Exception {
		addSubscription(SUBSCRIPTION_ID);
	}

	void addSubscription(String subscriptionId) throws Exception {
		String marketplaceId = marketplace == null ? null : marketplace
				.getMarketplaceId();
		subscription = Subscriptions.createSubscription(mgr, supplier, product,
				subscriptionId, marketplaceId, DateFactory.getInstance()
						.getTransactionTime(), DateFactory.getInstance()
						.getTransactionTime(), paymentInfo, billingContact, 1);
	}

	void addUsageLicense() throws Exception {
		usageLicense = Subscriptions
				.createUsageLicense(mgr, user, subscription);
		mgr.flush();
		usageLicense.setApplicationUserId(DomainObjectTestBase.USER_GUEST);
		usageLicense.setAssignmentDate(System.currentTimeMillis());
		mgr.flush();
	}

	void addUsageLicenseWithRoleDefinition() throws Exception {
		roleDefinition = TechnicalProducts.addRoleDefinition(
				ROLE_DEFINITION_ID, product.getTechnicalProduct(), mgr);
		subscription.addUser(user, roleDefinition);
		mgr.flush();
		usageLicense = subscription.getUsageLicenses().get(0);
		usageLicense.setApplicationUserId(DomainObjectTestBase.USER_GUEST);
		mgr.flush();
	}

	void addSubscription(boolean hasMarketplace, boolean hasPayment)
			throws Exception {
		addOrganization();
		addAndLoginAsUser();
		addProduct();
		if (hasMarketplace) {
			addMarketplace();
		}
		if (hasPayment) {
			addPaymentInfo();
			addBillingContact();
		}
		addSubscription();
	}

	private static void log(List<?> result) {
		StringBuilder sb = new StringBuilder();
		for (Object object : result) {
			sb.append("[");
			if (object instanceof Object[]) {
				for (Object column : (Object[]) object) {
					sb.append(column).append(", ");
				}
			} else {
				sb.append(object);
			}
			sb.append("]\n");
		}
		System.out.println(sb);
	}

	abstract class LogQueryRunner {

		public void run() throws Exception {
			final Date endDate = new Date();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR_OF_DAY, -1);
			final Date startDate = cal.getTime();
			runTX(new Callable<Void>() {
				public Void call() throws Exception {
					Query query = mgr.createNativeQuery(getQuery().getQuery());
					query.setParameter("startDate", startDate);
					query.setParameter("endDate", endDate);
					List<?> result = query.getResultList();
					log(result);
					assertResult(result);
					return null;
				}
			});
		}

		protected abstract void assertResult(List<?> result) throws Exception;
	}

	protected abstract UserOperationLogQuery getQuery();

}
