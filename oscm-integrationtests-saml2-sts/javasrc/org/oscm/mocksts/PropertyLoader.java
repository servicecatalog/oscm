/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 05.02.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.mocksts;

import java.util.Properties;

/**
 * Load properties from 'common.properties'
 * 
 * @author Wan Peng
 */
public class PropertyLoader {
    private final Properties prop;
    private static PropertyLoader PL_INSTANCE = null;

    private PropertyLoader() {
        prop = new Properties();
    }

    /**
     * Get a instance of PropertyLoader
     * 
     * @return Return a instance of PropertyLoader.
     */
    public static PropertyLoader getInstance() {
        if (PL_INSTANCE == null) {
            PL_INSTANCE = new PropertyLoader();
        }
        return PL_INSTANCE;
    }

    /**
     * Load property file.
     * 
     * @param file
     *            The property file will be loaded.
     * @return Return properties.
     */
    public Properties load(String file) {
        try {
            prop.load(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(file));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return prop;
    }
}
