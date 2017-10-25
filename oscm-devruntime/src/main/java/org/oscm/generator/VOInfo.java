/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class VOInfo {
    /**
     * Map of VO properties. For VO will be a special converter method
     * generated.
     */
    private HashMap<String, List<VOPropertyDescription>> voPropertiesInfo = new HashMap<String, List<VOPropertyDescription>>();
    /**
     * Set of lists of VOs. For this lists a special converter methods are
     * needed.
     */
    private HashSet<String> setOfVOLists = new HashSet<String>();

    /**
     * @param voPropertiesInfo
     *            the voPropertiesInfo to set
     */
    public void setVoPropertiesInfo(
            HashMap<String, List<VOPropertyDescription>> voPropertiesInfo) {
        this.voPropertiesInfo = voPropertiesInfo;
    }

    /**
     * @return the voPropertiesInfo
     */
    public HashMap<String, List<VOPropertyDescription>> getVoPropertiesInfo() {
        return voPropertiesInfo;
    }

    /**
     * @param setOfVOLists
     *            the setOfVOLists to set
     */
    public void setSetOfVOLists(HashSet<String> setOfVOLists) {
        this.setOfVOLists = setOfVOLists;
    }

    /**
     * @return the setOfVOLists
     */
    public HashSet<String> getSetOfVOLists() {
        return setOfVOLists;
    }
}
