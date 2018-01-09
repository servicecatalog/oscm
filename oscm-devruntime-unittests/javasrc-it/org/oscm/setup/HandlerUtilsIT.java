/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.setup;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Connection;
import java.util.Properties;

import org.junit.Assert;

import org.junit.Test;

public class HandlerUtilsIT {
    @Test(expected = FileNotFoundException.class)
    public void testReadPropertiesFromFile_NoFile() throws Exception {
        HandlerUtils.readProperties("c:/fake/file/path");
    }

    @Test
    public void testReadDbPropertiesFromFile_OK() throws Exception {
        File file = getFile("db.properties");
        Properties properties = HandlerUtils.readProperties(file
                .getAbsolutePath());
        Assert.assertNotNull(properties.getProperty("db.driver.class"));
        Assert.assertNotNull(properties.getProperty("db.name"));
        Assert.assertNotNull(properties.getProperty("db.user"));
        Assert.assertNotNull(properties.getProperty("db.pwd"));
        Assert.assertNotNull(properties.getProperty("db.type"));
    }

    @Test
    public void testReadMailPropertiesFromFile_OK() throws Exception {
        File file = getFile("un.properties");
        Properties properties = HandlerUtils.readProperties(file
                .getAbsolutePath());
        Assert.assertNotNull(properties.getProperty("MAIL_SERVER"));
        Assert.assertNotNull(properties.getProperty("MAIL_RESPONSE_ADDRESS"));
        Assert.assertNotNull(properties.getProperty("MAIL_BODY_en"));
    }

    @Test
    public void testEstablishDatabaseConnection_OK() throws Exception {
        File file = getFile("db.properties");
        Properties dbProperties = HandlerUtils.readProperties(file
                .getAbsolutePath());
        Connection dbConnection = HandlerUtils
                .establishDatabaseConnection(dbProperties);
        Assert.assertNotNull(dbConnection);
    }

    @Test(expected = RuntimeException.class)
    public void testEstablishDatabaseConnection_wrongSettings()
            throws Exception {
        File file = getFile("un.properties");
        Properties dbProperties = HandlerUtils.readProperties(file
                .getAbsolutePath());
        Connection dbConnection = HandlerUtils
                .establishDatabaseConnection(dbProperties);
        Assert.assertNotNull(dbConnection);
    }

    private File getFile(String name) {
        URL resource = this.getClass().getResource("/" + name);
        Assert.assertNotNull(resource);
        return new File(resource.getFile());
    }
}
