/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 21.10.2010                                                      
 *                                                                              
 *  Completion Time: 21.10.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * A UI model row object for creating/editing UDAs.
 * 
 * @author weiser
 * 
 */
public class UdaRow implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String HIDDEN_PWD = "*****";

    /**
     * Maps the existing {@link VOUda}s to their {@link VOUdaDefinition}s and
     * creates new {@link VOUda}s for {@link VOUdaDefinition}s without. Creates
     * a list of {@link UdaRow}s for table operations in the UI.
     * 
     * @param definitons
     *            the list of available uda definitions
     * @param udas
     *            the list of existing udas
     * @return
     */
    public static List<UdaRow> getUdaRows(List<VOUdaDefinition> definitons,
            List<VOUda> udas) {
        List<UdaRow> result = new ArrayList<>();
        if (definitons == null) {
            return result;
        }
        for (VOUdaDefinition def : definitons) {
            VOUda uda = getUdaForDefinition(def, udas);
            if (uda == null) {
                uda = new VOUda();
                uda.setUdaDefinition(def);
            }
            UdaRow udaRow = new UdaRow(def, uda);
            udaRow.initPasswordValueToStore();
            result.add(udaRow);
        }
        return result;
    }

    /**
     * Tries to find a {@link VOUda} in a list of existing ones for a
     * {@link VOUdaDefinition}.
     * 
     * @param def
     *            the {@link VOUdaDefinition} the get the {@link VOUda} for
     * @param udas
     *            the list of existing {@link VOUda}s to search in.
     * @return the found {@link VOUda} or <code>null</code>
     */
    private static VOUda getUdaForDefinition(VOUdaDefinition def,
            List<VOUda> udas) {
        if (udas == null) {
            return null;
        }
        for (VOUda uda : udas) {
            if (uda.getUdaDefinition().getKey() == def.getKey()) {
                return uda;
            }
        }
        return null;
    }

    /**
     * decide if an input field has to be rendered for the UDA value
     */
    public boolean isInputRendered() {
        return udaDefinition.getConfigurationType() == UdaConfigurationType.SUPPLIER;
    }

    /**
     * decide if a label has to be rendered for the UDA value
     */
    public boolean isLabelRendered() {
        return !isInputRendered();
    }

    /**
     * decide if an input field is mandatory
     */
    public boolean isInputMandatory() {
        return udaDefinition.getConfigurationType() == UdaConfigurationType.USER_OPTION_MANDATORY;
    }

    /**
     * decide if an input field is encrypted
     */
    public boolean isInputEncrypted() {
        return udaDefinition.isEncrypted();
    }

    private VOUdaDefinition udaDefinition;
    private VOUda uda;
    private VOOrganization vendor;
    private String passwordValueToStore;

    public UdaRow(VOUdaDefinition voUdaDefinition, VOUda voUda,
            VOOrganization voVendor) {
        udaDefinition = voUdaDefinition;
        uda = voUda;
        vendor = voVendor;
        // apply the default value only for new UDAs
        if (uda.getUdaValue() == null) {
            uda.setUdaValue(udaDefinition.getDefaultValue());
        }
    }

    public UdaRow(VOUdaDefinition voUdaDefinition, VOUda voUda) {
        this(voUdaDefinition, voUda, null);
    }

    public String getUdaId() {
        return udaDefinition.getUdaId();
    }

    public String getUdaNameToShow() {
        if (StringUtils.isNoneBlank(udaDefinition.getName())) {
            return udaDefinition.getName();
        }
        return udaDefinition.getUdaId();
    }

    public String getUdaValue() {
        return uda.getUdaValue();
    }

    public void setUdaValue(String udaValue) {
        uda.setUdaValue(udaValue);
    }

    public VOUdaDefinition getUdaDefinition() {
        return udaDefinition;
    }

    public VOUda getUda() {
        return uda;
    }

    public VOOrganization getVendor() {
        return vendor;
    }

    public void setVendor(VOOrganization vendor) {
        this.vendor = vendor;
    }

    public String getPasswordValueToStore() {
        return passwordValueToStore;
    }

    public void setPasswordValueToStore(String passwordValueToStore) {
        this.passwordValueToStore = passwordValueToStore;
    }

    public void rewriteEncryptedValues() {
        if (!this.isInputEncrypted()) {
            return;
        }
        if (this.getPasswordValueToStore() == null
                || !this.getPasswordValueToStore().trim().equals(HIDDEN_PWD)) {
            this.setUdaValue(this.getPasswordValueToStore());
        }
    }

    public void initPasswordValueToStore() {
        if (!this.isInputEncrypted()) {
            return;
        }
        if (StringUtils.isNotBlank(this.getUdaValue())) {
            this.setPasswordValueToStore(HIDDEN_PWD);
        } else {
            this.setPasswordValueToStore("");
        }
    }

    public String getUdaValueToShow() {
        if (this.isInputEncrypted()) {
            return HIDDEN_PWD;
        } else {
            return this.getUdaValue();
        }
    }
}
