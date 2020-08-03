/**
 * ***************************************************************************** Copyright FUJITSU
 * LIMITED 2018 *****************************************************************************
 */
package org.oscm.internal.portallandingpage;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOService;

@Remote
public interface LandingpageService {
  /**
   * Returns the list of services displayed on the landing page of the given marketplace. The
   * landing page configuration defines the maximum number of services as well as a criterion
   * according to which the page is filled up if not enough featured services are available.
   *
   * <p>If the calling user is logged in, the list only includes services which are visible to him
   * and his organization. Otherwise, the list only includes services visible to anonymous
   * (non-registered) users.
   *
   * <p>Required role: none
   *
   * @param marketplaceId the ID of the marketplace
   * @param locale the language in which service details are to be returned. Specify a language code
   *     as returned by <code>getLanguage()</code> of <code>java.util.Locale</code>.
   * @return the list of services
   */
  public List<VOService> servicesForLandingpage(String marketplaceId, String locale)
      throws ObjectNotFoundException;

  /**
   * Loads the images of the given services which the specified supplier has defined for the
   * marketplace associated with the service.
   *
   * <p>
   *
   * @param services the services of the supplier
   * @return a <code>Map<VOService, VOImageResource></code> object where the key is the VOService
   *     and the value the VOImageResource
   * @throws ObjectNotFoundException if the supplier is not found
   */
  public Map<Long, VOImageResource> fillInServiceImages(List<VOService> services);
}
