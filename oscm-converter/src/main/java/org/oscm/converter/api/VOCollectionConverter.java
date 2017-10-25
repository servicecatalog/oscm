/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 20.02.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.dataservice.local.DataService;

public class VOCollectionConverter {

    public static <T, S> List<T> convertList(List<S> sourceList,
            Class<T> targetType) {

        return convertList(sourceList, targetType, null);
    }

    public static <T, S> List<T> convertList(List<S> sourceList,
            Class<T> targetType, DataService ds) {

        if (sourceList == null) {
            return null;
        }

        List<T> targetList = new ArrayList<T>();
        for (S sourceVo : sourceList) {
            T targetVo = targetType.cast(VOConverter
                    .reflectiveConvert(sourceVo, ds));
            if (targetVo != null) {
                targetList.add(targetVo);
            }
        }

        return targetList;
    }

    public static <T, S> Set<T> convertSet(Set<S> sourceSet, Class<T> targetType) {

        if (sourceSet == null) {
            return null;
        }

        Set<T> targetSet = new HashSet<T>();
        for (S sourceVo : sourceSet) {
            T targetVo = targetType.cast(VOConverter
                    .reflectiveConvert(sourceVo));
            if (targetVo != null) {
                targetSet.add(targetVo);
            }
        }

        return targetSet;
    }

}
