/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 02.07.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.setup;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author Mike J&auml;ger
 * 
 */
public class DatabaseAntTaskDropTables extends Task {

    private String dbDriverClassName;
    private String databaseDriverURL;
    private String databaseUserName;
    private String databaseUserPwd;
    private String deleteDbXMLFilePath;

    public void setDbDriverClassName(String dbDriverClassName) {
        this.dbDriverClassName = dbDriverClassName;
    }

    public void setDatabaseDriverURL(String databaseDriverURL) {
        this.databaseDriverURL = databaseDriverURL;
    }

    public void setDatabaseUserName(String databaseUserName) {
        this.databaseUserName = databaseUserName;
    }

    public void setDatabaseUserPwd(String databaseUserPwd) {
        this.databaseUserPwd = databaseUserPwd;
    }

    public void setDeleteDbXMLFilePath(String deleteDbXMLFilePath) {
        this.deleteDbXMLFilePath = deleteDbXMLFilePath;
    }

    public void execute() throws BuildException {
        try {
            DatabaseTaskHandler.init(dbDriverClassName, databaseDriverURL,
                    databaseUserName, databaseUserPwd);
            DatabaseTaskHandler.dropTables(deleteDbXMLFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }

    }

}
