/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *******************************************************************************/
package org.oscm.test.db;

import javax.sql.DataSource;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Interface for test database creation. The interface decouples the tests from
 * different test database implementations (in-memory, DB server etc).
 * 
 * @author hoffmann
 */
public interface ITestDB {

    /**
     * Initializes the DB with the current schema and clears all instance
     * content.
     */
    public void initialize() throws Exception;

    /**
     * Loads all schemas up to the latest version.
     * 
     * @throws Exception
     */
    public void loadSchema() throws Exception;

    /**
     * Loads all schemas up to the specified version.
     * 
     * @param toVersion
     * @param out
     * @throws Exception
     */
    public void loadSchema(DatabaseVersionInfo toVersion) throws Exception;

    /**
     * Cleans all DB content including the schema.
     * 
     * @throws Exception
     */
    public void purgeSchema() throws Exception;

    /**
     * @return data source instance for this test DB
     */
    public DataSource getDataSource();

    /**
     * Deletes all instance content except database version information.
     * 
     * @throws Exception
     */
    public void clearBusinessData() throws Exception;

}
