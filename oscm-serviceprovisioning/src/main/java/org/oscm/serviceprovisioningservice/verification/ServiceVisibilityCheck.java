/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Oliver Soehnges                                                      
 *                                                                              
 *  Creation Date: 18.05.2011                                                      
 *                                                                              
 *  Completion Time: 18.05.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.verification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceOperationException.Reason;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOMarketplace;

/**
 * Class to ensure the constraint for the visible-in-catalog property of catalog
 * entries:
 * <p>
 * For every technical service which is offered through an active service within
 * a global marketplace, at least one visible-in-catalog service must exists.
 * <p>
 * Unfortunately this can't be verified through a query only, because parts of
 * the changes might not be persisted if trigger functions have been registered
 * for the activation/deactivation process. Therefore the results of the query
 * must be combined with the given activation and deactivation requests.
 * 
 * @author Soehnges
 */
public class ServiceVisibilityCheck {
	private static final Log4jLogger logger = LoggerFactory
			.getLogger(ServiceVisibilityCheck.class);

	private final DataService dm;

	// Caches all affected technical products
	private final Set<TechnicalProduct> technicalProducts = new HashSet<TechnicalProduct>();

	// Caches all transient trigger operations
	private final Map<Long, List<TriggerData>> triggerOperations = new HashMap<Long, List<TriggerData>>();

	public ServiceVisibilityCheck(DataService dm) {
		this.dm = dm;
	}

	/**
	 * Just override existing TriggerData objects, since the trigger has higher
	 * priority and will change the state accordingly.
	 * 
	 * @param map
	 *            Input and updated output
	 * @param triggers
	 *            The triggers for the technical product
	 * @param product
	 */
	private void mergeActiveServicesWithTriggers(
			Map<Marketplace, Map<Long, TriggerData>> map,
			List<TriggerData> triggers) {
		if (triggers != null) {
			for (TriggerData triggerData : triggers) {
				try {
					final Marketplace marketplace = dm.getReference(
							Marketplace.class, triggerData.marketplaceKey);
					if (!map.containsKey(marketplace)) {
						map.put(marketplace, new HashMap<Long, TriggerData>());
					}
					final Map<Long, TriggerData> catalogEntries = map
							.get(marketplace);
					catalogEntries.put(Long.valueOf(triggerData.serviceKey),
							triggerData);
				} catch (ObjectNotFoundException ex) {
					// Deleted Marketplace? Is not allowed to happen!
					SaaSSystemException e = new SaaSSystemException(ex);
					logger.logError(
							LogMessageIdentifier.ERROR_MARKETPLACE_NOT_FOUND,
							"" + triggerData.marketplaceKey);
					throw e;
				}
			}
		}
	}

	/**
	 * Here we are dealing with one technical product on one marketplace. Things
	 * are easy because we just have to find out if at least one product is
	 * visible and active.
	 * 
	 * @param activeServices
	 *            all services, active and triggered ones
	 * @return true if one service has been found
	 */
	private boolean hasOrWillHaveVisibleServices(
			Map<Long, TriggerData> activeServices) {
		if (activeServices.isEmpty()) {
			// no service on marketplace (should not occur)
			return true;
		}
		Iterator<TriggerData> catalogEntries = activeServices.values()
				.iterator();
		while (catalogEntries.hasNext()) {
			final TriggerData catalogEntry = catalogEntries.next();
			if (catalogEntry.visibleInCatalog && catalogEntry.active) {
				// Condition is valid => we found an active
				// service which is visible!
				return true;
			}
		}
		// see if all services are deactivated. If not, return false
		catalogEntries = activeServices.values().iterator();
		while (catalogEntries.hasNext()) {
			if (catalogEntries.next().active) {
				// Condition is valid, at least one service is still active.
				// Since no active service is visible, return false
				return false;
			}
		}
		// all services deactivated, return true
		return true;
	}

	/**
	 * Validates whether constraint about visibility and activation state is
	 * fulfilled.
	 * <p>
	 * If not an exception will be thrown.
	 */
	public void validate() throws ServiceOperationException {
		// Get current supplier
		final Organization supplier = dm.getCurrentUser().getOrganization();
		Marketplace marketplace = null;

		// Process all affected technical products
		for (TechnicalProduct product : technicalProducts) {
			// First of all we get all global published products of this
			// technical product which are currently defined as active.
			final Map<Marketplace, Map<Long, TriggerData>> servicesOnMpls = getActiveServicesForTechnicalProduct(
					product, supplier);
			mergeActiveServicesWithTriggers(servicesOnMpls,
					triggerOperations.get(Long.valueOf(product.getKey())));

			// For each marketplace check if there is at least one active and
			// visible product
			for (Iterator<Marketplace> i = servicesOnMpls.keySet().iterator(); i
					.hasNext();) {
				marketplace = i.next();
				if (!hasOrWillHaveVisibleServices(servicesOnMpls
						.get(marketplace))) {
					// bad luck, have to throw an exception here: at least one
					// service is active, none of them are visible
					final ServiceOperationException sof = new ServiceOperationException(
							Reason.NO_VISIBLE_ACTIVE_SERVICE);
					sof.setMessageParams(new String[] {
							product.getTechnicalProductId(),
							marketplace.getMarketplaceId() });
					logger.logWarn(
							Log4jLogger.SYSTEM_LOG,
							sof,
							LogMessageIdentifier.WARN_NO_VISIBLE_GLOBAL_SERVICE_ASSIGNED,
							Long.toString(product.getKey()));
					throw sof;
				}
			}
		}
	}

	/**
	 * This methods retrieves all active products which are based on the passed
	 * technical product and owned by the passed supplier organization for all
	 * marketplaces.
	 * 
	 * @param tp
	 *            the technical product on which the products are based.
	 * 
	 * @param supplier
	 *            the organization which owns the products.
	 * @return a structure which relates the active products too the marketplace
	 *         on which it is published.
	 */
	private Map<Marketplace, Map<Long, TriggerData>> getActiveServicesForTechnicalProduct(
			TechnicalProduct tp, Organization supplier) {

		// First get all active products on all marketplaces which are based on
		// the passed tp for the passed supplier by query.
		final Query query = dm
				.createNamedQuery("Product.getActiveProductsForMarketplace");
		query.setParameter("technicalProduct", tp);
		query.setParameter("vendorKey", Long.valueOf(supplier.getKey()));
		final List<CatalogEntry> prodVisList = ParameterizedTypes.list(
				query.getResultList(), CatalogEntry.class);

		// Now structure the result of the query, so that all products which are
		// published on the same marketplace are grouped together
		// The resulting map contains for every marketplace every active
		// product which is based on the current technical service.
		final Map<Marketplace, Map<Long, TriggerData>> productsOnMarketplaces = new HashMap<Marketplace, Map<Long, TriggerData>>();
		for (CatalogEntry catalogEntry : prodVisList) {
			Marketplace marketplace = catalogEntry.getMarketplace();
			if (!productsOnMarketplaces.containsKey(marketplace)) {
				productsOnMarketplaces.put(marketplace,
						new HashMap<Long, TriggerData>());
			}
			productsOnMarketplaces.get(marketplace).put(
					Long.valueOf(catalogEntry.getProduct().getKey()),
					new TriggerData(catalogEntry.getProduct().getKey(),
							catalogEntry.getMarketplace().getKey(),
							catalogEntry.isVisibleInCatalog(), true));
		}
		return productsOnMarketplaces;
	}

	/**
	 * Adds an activation or deactivation request (including informations about
	 * visibility changes).
	 * <p>
	 * If <code>entries</code> is not <code>null</code> or empty, the operation
	 * is not yet persisted within the database! In this case we handle a
	 * trigger.
	 */
	public void add(Product service, List<VOCatalogEntry> entries,
			boolean active) {
		// Remember technical product
		technicalProducts.add(service.getTechnicalProduct());

		// entries != null than we deal with triggers
		// skip if customer specific service
		if (entries != null && entries.size() > 0
				&& service.getTargetCustomer() == null) {
			// Ok! => remember triggered service operation
			final Long tpKey = Long.valueOf(service.getTechnicalProduct()
					.getKey());
			if (!triggerOperations.containsKey(tpKey)) {
				triggerOperations.put(tpKey, new ArrayList<TriggerData>());
			}
			for (VOCatalogEntry catalogEntry : entries) {
				VOMarketplace mp = catalogEntry.getMarketplace();
				if (mp != null) {
					triggerOperations.get(tpKey).add(
							new TriggerData(service.getKey(), mp.getKey(),
									catalogEntry.isVisibleInCatalog(), active));
				}
			}
		}
	}

	private class TriggerData {
		private final long serviceKey;
		private final long marketplaceKey;
		private final boolean visibleInCatalog;
		// this flag is only used for triggered service activations which will
		// deactivate the service, otherwise it is true anyway
		private final boolean active;

		public TriggerData(long serviceKey, long marketplaceKey,
				boolean visibleInCatalog, boolean activation) {
			this.serviceKey = serviceKey;
			this.marketplaceKey = marketplaceKey;
			this.visibleInCatalog = visibleInCatalog;
			this.active = activation;
		}
	}

}
