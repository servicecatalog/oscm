/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Aleh Khomich                                                     
 *                                                                              
 *  Creation Date: 22.06.2010                                                      
 *                                                                              
 *  Completion Time: 22.06.2010                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.webservices;

import java.util.List;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOCollectionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.intf.VatService;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOCountryVatRate;
import org.oscm.vo.VOOrganizationVatRate;
import org.oscm.vo.VOVatRate;
import org.oscm.webservices.logger.WebServiceLogger;

/**
 * End point facade for WS.
 * 
 * @author pock
 * 
 */
@WebService(name = "VatService", serviceName = "VatService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.VatService")
public class VatServiceWS implements VatService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(VatServiceWS.class));

    org.oscm.internal.intf.VatService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public List<VOCountryVatRate> getCountryVats() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(delegate.getCountryVats(),
                org.oscm.vo.VOCountryVatRate.class);
    }

    @Override
    public VOVatRate getDefaultVat() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOConverter.convertToApi(delegate.getDefaultVat());
    }

    @Override
    public List<VOOrganizationVatRate> getOrganizationVats() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(delegate.getOrganizationVats(),
                org.oscm.vo.VOOrganizationVatRate.class);
    }

    @Override
    public boolean getVatSupport() {
        WS_LOGGER.logAccess(wsContext, ds);
        return delegate.getVatSupport();
    }

    @Override
    public void saveAllVats(VOVatRate defaultVat,
            List<VOCountryVatRate> countryVats,
            List<VOOrganizationVatRate> organizationVats)
            throws OrganizationAuthoritiesException,
            ConcurrentModificationException, OperationNotPermittedException,
            ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.saveAllVats(VOConverter.convertToUp(defaultVat),
                    VOCollectionConverter.convertList(countryVats,
                            org.oscm.internal.vo.VOCountryVatRate.class),
                    VOCollectionConverter.convertList(organizationVats,
                            org.oscm.internal.vo.VOOrganizationVatRate.class));
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void saveCountryVats(List<VOCountryVatRate> countryVats)
            throws OrganizationAuthoritiesException,
            ConcurrentModificationException, ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.saveCountryVats(VOCollectionConverter.convertList(
                    countryVats, org.oscm.internal.vo.VOCountryVatRate.class));
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void saveDefaultVat(VOVatRate defaultVat)
            throws OrganizationAuthoritiesException,
            ConcurrentModificationException, ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.saveDefaultVat(VOConverter.convertToUp(defaultVat));
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void saveOrganizationVats(
            List<VOOrganizationVatRate> organizationVats)
            throws OrganizationAuthoritiesException,
            ConcurrentModificationException, OperationNotPermittedException,
            ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.saveOrganizationVats(
                    VOCollectionConverter.convertList(organizationVats,
                            org.oscm.internal.vo.VOOrganizationVatRate.class));
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

}
