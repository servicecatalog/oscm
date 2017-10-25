/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.setup;

import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Utility to create a documented default configuration file. Used during the
 * build.
 * 
 * @author hoffmann
 */
public class DefaultConfigFileCreator {

    public static void main(String[] args) throws Exception {
        ConfigurationKey.printExampleConfig(System.out);
    }

}
