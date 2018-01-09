/*
 *  Copyright FUJITSU LIMITED 2018
 */

package org.oscm.integrationhelper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.junit.Test;

public class PropertyFileReaderTest {
    @Test
    public void getPropertiesFromFile_fileNameNull() {
        // given
        String fileName = null;

        // when
        Properties result = PropertyFileReader.getPropertiesFromFile(fileName);

        // then
        assertNull(result);
    }

    @Test
    public void getPropertiesFromFile_fileDoesNotExist() {
        // given
        String fileName = "not_existing_file";

        // when
        Properties result = PropertyFileReader.getPropertiesFromFile(fileName);

        // then
        assertNull(result);
    }

    @Test
    public void getPropertiesFromFile_fileExist() {
        // given
        String fileName = "test_sts.properties";

        // when
        Properties result = PropertyFileReader.getPropertiesFromFile(fileName);

        // then
        assertNotNull(result);
    }
}
