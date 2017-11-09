/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 23.11.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;

import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.converter.BigDecimalConverter;
import org.oscm.ui.model.Organization;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.intf.VatService;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCountryVatRate;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationVatRate;
import org.oscm.internal.vo.VOVatRate;

/**
 * Backing bean for VAT related actions
 * 
 */
@ViewScoped
@ManagedBean(name = "vatBean")
public class VatBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 3535699653824474664L;

    private final BigDecimalConverter bigDecimalConverter = new BigDecimalConverter();

    private VatService vatService;

    private boolean dirty;

    private Boolean vatEnabled;

    private VOVatRate defaultVat;

    private List<VOCountryVatRate> countryVats;

    private List<String> countries;

    private List<VOOrganizationVatRate> customerVats;

    private List<VOOrganization> customers;

    private Map<String, String> customerNames;

    private Map<VOVatRate, String> vatStrings;

    @ManagedProperty(value = "#{countryBean}")
    private CountryBean countryBean;

    /**
     * @return the VAT service
     */
    private VatService getVatService() {
        vatService = getService(VatService.class, vatService);
        return vatService;
    }

    public CountryBean getCountryBean() {
        return countryBean;
    }

    public void setCountryBean(CountryBean countryBean) {
        this.countryBean = countryBean;
    }

    public VOVatRate getDefaultVat() {
        if (vatEnabled == null) {
            defaultVat = getVatService().getDefaultVat();
            addToVatStrings(defaultVat);
            vatEnabled = defaultVat != null;
        }
        return defaultVat;
    }

    public boolean isdirty() {
        return dirty;
    }

    public Boolean getVatEnabled() {
        getDefaultVat();
        return vatEnabled;
    }

    public void setVatEnabled(Boolean vatEnabled) {
        reset();
        dirty = !vatEnabled.equals(getVatEnabled());
        if (vatEnabled.booleanValue()) {
            if (getDefaultVat() == null) {
                defaultVat = new VOVatRate();
                addToVatStrings(defaultVat);
            }
        } else {
            defaultVat = null;
            try {
                countryVats = new ArrayList<>();
                countries = getAccountingService().getSupportedCountryCodes();
                if (countries.size() > 0) {
                    countryVats.add(new VOCountryVatRate());
                }
                customerVats = new ArrayList<>();
                customers = getAccountingService().getMyCustomersOptimization();
                if (customers.size() > 0) {
                    customerVats.add(new VOOrganizationVatRate());
                }
                customerNames = new HashMap<>();
            } catch (SaaSApplicationException e) {
                ExceptionHandler.execute(e);
            }
        }
        this.vatEnabled = vatEnabled;
    }

    private void initCountryMembers() {
        countryVats = getVatService().getCountryVats();

        countries = getAccountingService().getSupportedCountryCodes();
        Collections.sort(countries,
                countryBean.new DefaultSortingOfCountryCodes());
        addCountryVat();
    }

    public List<VOCountryVatRate> getCountryVats() {
        if (countryVats == null) {
            initCountryMembers();
        }
        return countryVats;
    }

    public List<String> getCountries() {
        if (countries == null) {
            initCountryMembers();
        }
        return countries;
    }

    private void initCustomerMembers() {
        try {
            customerVats = getVatService().getOrganizationVats();

            customers = getAccountingService().getMyCustomersOptimization();

            customerNames = new HashMap<>();
            for (VOOrganization customer : customers) {
                customerNames.put(customer.getOrganizationId(),
                        Organization.getNameWithOrganizationId(customer));
            }

            addCustomerVat();
        } catch (OrganizationAuthoritiesException e) {
            ExceptionHandler.execute(e);
        }
    }

    public List<VOOrganizationVatRate> getCustomerVats() {
        if (customerVats == null) {
            initCustomerMembers();
        }
        return customerVats;
    }

    public List<VOOrganization> getCustomers() {
        if (customers == null) {
            initCustomerMembers();
        }
        return customers;
    }

    public Map<String, String> getCustomerNames() {
        if (customerNames == null) {
            initCustomerMembers();
        }
        return customerNames;
    }

    public Map<VOVatRate, String> getVatStrings() {
        if (vatStrings == null) {
            vatStrings = new HashMap<>();
            VOVatRate defaultVat = getDefaultVat();
            if (defaultVat != null) {
                addToVatStrings(getDefaultVat());
            }
            addToVatStrings(getCustomerVats());
            addToVatStrings(getCountryVats());
        }
        return vatStrings;
    }

    private void addToVatStrings(List<? extends VOVatRate> vats) {
        for (VOVatRate vat : vats) {
            addToVatStrings(vat);
        }
    }

    private void addToVatStrings(VOVatRate vat) {
        if (vat == null) {
            return;
        }
        getVatStrings()
                .put(vat,
                        bigDecimalConverter.getAsString(
                                FacesContext.getCurrentInstance(), null,
                                vat.getRate()));
    }

    public void addCountryVat() {
        this.addCountryVat(null);
    }

    /**
     * Also called from client via AJAX to add a new row to the table with the
     * country specific VAT rates
     * 
     */
    @SuppressWarnings("unused")
    public void addCountryVat(final AjaxBehaviorEvent event) {
        // remove any used country from the countries list
        final List<String> beanCountries = this.getCountries();
        final List<VOCountryVatRate> countryVatRates = this.getCountryVats();

        for (VOCountryVatRate voCountryVatRate : countryVatRates) {
            if (voCountryVatRate != null) {
                beanCountries.remove(voCountryVatRate.getCountry());
            }
        }

        if (beanCountries.size() > 0) {
            final VOCountryVatRate vo = new VOCountryVatRate();
            countryVatRates.add(vo);
            addToVatStrings(vo);
        }
    }

    /**
     * Also called from client via AJAX to add a new row to the table with the
     * organization specific VAT rates
     * 
     */
    public void addCustomerVat() {
        // remove any used customer from the customers list
        Set<String> usedCustomers = new HashSet<>();
        for (VOOrganizationVatRate vat : getCustomerVats()) {
            usedCustomers.add(vat.getOrganization().getOrganizationId());
        }
        Iterator<VOOrganization> it = getCustomers().iterator();
        while (it.hasNext()) {
            String id = it.next().getOrganizationId();
            if (usedCustomers.contains(id)) {
                it.remove();
            }
        }

        if (getCustomers().size() > 0) {
            VOOrganizationVatRate vo = new VOOrganizationVatRate();
            vo.setOrganization(new VOOrganization());
            getCustomerVats().add(vo);
            addToVatStrings(vo);
        }
    }

    private BigDecimal getASBigDecimal(String value) {
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        return (BigDecimal) bigDecimalConverter.getAsObject(
                FacesContext.getCurrentInstance(), null, value);
    }

    public void validationVat(final FacesContext context,
            final UIComponent toValidate, final Object value) {
        if (context.getExternalContext().getRequestParameterMap()
                .containsKey("vatForm:saveButton")
                && StringUtils.isNotBlank((String) value)) {
            try {
                Object obj = bigDecimalConverter.getAsObject(context,
                        toValidate, (String) value);
                if (!ADMValidator.isVat((BigDecimal) obj)) {
                    Object[] args = null;
                    String label = JSFUtils.getLabel(toValidate);
                    if (label != null) {
                        args = new Object[] { label };
                    }
                    ValidationException e = new ValidationException(
                            ValidationException.ReasonEnum.VAT, label, null);
                    String text = JSFUtils.getText(e.getMessageKey(), args,
                            context);
                    throw new ValidatorException(new FacesMessage(
                            FacesMessage.SEVERITY_ERROR, text, null));
                }
            } catch (ConverterException e) {
                throw new ValidatorException(e.getFacesMessage());
            }
        }
    }

    /**
     * Load the VAT rates from the service
     * 
     */
    private void reset() {

        dirty = false;

        vatEnabled = null;
        defaultVat = null;

        countryVats = null;
        countries = null;

        customerVats = null;
        customers = null;

        vatStrings = null;

    }

    /**
     * Save all VAT rates
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     *             Thrown from the business logic.
     */
    public String save() throws SaaSApplicationException {

        VOVatRate defaultVat = getDefaultVat();
        try {
            if (defaultVat == null) {
                getVatService().saveAllVats(null, null, null);
            } else {
                defaultVat.setRate(getASBigDecimal(getVatStrings().get(
                        defaultVat)));
                for (Iterator<VOCountryVatRate> it = getCountryVats()
                        .iterator(); it.hasNext();) {
                    VOCountryVatRate vat = it.next();
                    final String country = vat.getCountry();
                    if (country == null || country.isEmpty()) {
                        it.remove();
                    } else {
                        vat.setRate(getASBigDecimal(getVatStrings().get(vat)));
                    }
                }
                for (Iterator<VOOrganizationVatRate> it = getCustomerVats()
                        .iterator(); it.hasNext();) {
                    VOOrganizationVatRate vat = it.next();
                    final String organizationId = vat.getOrganization().getOrganizationId();
                    if (organizationId == null || organizationId.isEmpty()) {
                        it.remove();
                    } else {
                        vat.setRate(getASBigDecimal(getVatStrings().get(vat)));
                    }
                }
                getVatService().saveAllVats(defaultVat, countryVats,
                        customerVats);
            }
            addMessage(null, FacesMessage.SEVERITY_INFO, INFO_VAT_SAVED);
        } catch (ConverterException e) {
            // this should not happen because we already validated the
            // values
            FacesContext.getCurrentInstance().addMessage(null,
                    e.getFacesMessage());
        } finally {
            reset();
        }

        return OUTCOME_SUCCESS;
    }

}
