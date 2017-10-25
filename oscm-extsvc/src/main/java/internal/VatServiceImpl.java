/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package internal;

import java.util.List;

import javax.jws.WebService;

import org.oscm.intf.VatService;
import org.oscm.vo.VOCountryVatRate;
import org.oscm.vo.VOOrganizationVatRate;
import org.oscm.vo.VOVatRate;

/**
 * This is a stub implementation of the {@link VatService} as the Metro jax-ws
 * tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author pock
 */

@WebService(serviceName = "VatService", targetNamespace = "http://oscm.org/xsd", portName = "VatServicePort", endpointInterface = "org.oscm.intf.VatService")
public class VatServiceImpl implements VatService {

    @Override
    public List<VOCountryVatRate> getCountryVats() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOVatRate getDefaultVat() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganizationVatRate> getOrganizationVats() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getVatSupport() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAllVats(VOVatRate defaultVat,
            List<VOCountryVatRate> countryVats,
            List<VOOrganizationVatRate> organizationVats) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveCountryVats(List<VOCountryVatRate> countryVats) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveDefaultVat(VOVatRate defaultVat) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveOrganizationVats(
            List<VOOrganizationVatRate> organizationVats) {
        throw new UnsupportedOperationException();
    }

}
