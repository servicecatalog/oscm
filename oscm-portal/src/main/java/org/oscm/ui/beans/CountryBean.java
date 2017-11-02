/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

/**
 * Backing bean for country related actions
 * 
 */
@ViewScoped
@ManagedBean(name="countryBean")
public class CountryBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 1834751011680299409L;

    /**
     * By default, country codes are sorted alphabetical according to the
     * display name.
     */
    public class DefaultSortingOfCountryCodes implements Comparator<String> {

        Locale userLocale = getCurrentUserLocale();

        Collator collator = Collator.getInstance(userLocale);

        public int compare(String countryCode1, String countryCode2) {
            return collator.compare(
                    getDisplayCountry(countryCode1, userLocale),
                    getDisplayCountry(countryCode2, userLocale));
        }

    }

    /**
     * List of all supported countries of the logged in organization
     */
    private List<String> supportedCountries;

    /**
     * The locale of the user. Can be set by unit test.
     */
    Locale currentUserLocale;

    /**
     * The locale of the last access. Needed to detect locale changes in a
     * single request.
     */
    Locale lastUserLocale;

    /**
     * Mapping from country codes in ISO 3166 to localized country names.
     */
    private Map<String, String> displayCountries = new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        /**
         * Returns the localized country name for the given country code. In
         * case no localized country name exists, then the country code is
         * returned.
         */
        @Override
        public String get(Object countryCode) {
            String displayName = super.get(countryCode);
            return (displayName != null) ? displayName : (String) countryCode;
        }

    };

    /**
     * Returns all country codes this customer can choose from. The list of
     * supported country codes is defined by the platform.
     * 
     * @return List of country codes in ISO 3166
     */
    public List<String> getSupportedCountryCodesForPublicRegistrationPage() {
        currentUserLocale = FacesContext.getCurrentInstance().getViewRoot()
                .getLocale();
        return getSupportedCountryCodes();
    }

    /**
     * Returns the country codes the customer can choose from. The list of
     * supported country codes is defined by the platform.
     * 
     * @return List of country codes in ISO 3166
     */
    public List<String> getSupportedCountryCodesForCustomer() {
        return getSupportedCountryCodes();
    }

    /**
     * Returns all country codes this organization can choose from. The list of
     * supported country codes is defined by the platform.
     * 
     * @return List of country codes in ISO 3166
     */
    public List<String> getSupportedCountryCodesForEditProfilePage() {
        return getSupportedCountryCodes();
    }

    /*
     * Returns all country codes in ISO 3166 that are supported by the given
     * supplier.
     * 
     * @param supplierID or null if the 'parent' organization of the login
     * organization is to be used.
     * 
     * @return List of country codes in ISO 3166
     */
    List<String> getSupportedCountryCodes() {
        if (hasLocaleChanged()) {
            reset();
        }
        if (supportedCountries == null) {
            supportedCountries = getAccountingService()
                    .getSupportedCountryCodes();
            Collections.sort(supportedCountries,
                    new DefaultSortingOfCountryCodes());
        }
        return supportedCountries;
    }

    /**
     * Returns a mapping from country codes in ISO 3166 to localized country
     * names.
     */
    public Map<String, String> getDisplayCountries() {
        if (hasLocaleChanged()) {
            reset();
        }
        if (displayCountries.isEmpty()) {
            Locale userLocale = getCurrentUserLocale();
            for (String code : Locale.getISOCountries()) {
                String country = getDisplayCountry(code, userLocale);
                displayCountries.put(code, country);
            }
        }
        return displayCountries;
    }

    /*
     * Checks if the user locale has changed during the same single request.
     * (e.g. the user updated the locale in the profile settings)
     */
    boolean hasLocaleChanged() {
        Locale currentLocale = getCurrentUserLocale();
        boolean changed = !currentLocale.equals(lastUserLocale);
        lastUserLocale = currentLocale;
        return changed;
    }

    /*
     * Returns the display name of the given country code
     */
    String getDisplayCountry(String code, Locale userLocale) {
        String country = new Locale("", code).getDisplayCountry(userLocale);
        return country;
    }

    /*
     * Returns the locale of the user.
     */
    Locale getCurrentUserLocale() {
        if (currentUserLocale != null) {
            return currentUserLocale;
        }
        Locale currentLocale = new Locale(getUserFromSession().getLocale());
        return currentLocale;
    }

    /**
     * Clears all cached data.
     */
    public void reset() {
        displayCountries.clear();
        supportedCountries = null;
    }

}
