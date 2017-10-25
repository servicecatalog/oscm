/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 25.03.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.setup;

import javax.swing.JOptionPane;

/**
 * @author pock
 * 
 */
public class DatabaseInit {

    private static final String DATABASE_INIT_XML = "../javares/databaseinit.xml";
    private final static String ROOT_PATH = "../../../../../";

    public static void main(String args[]) throws Exception {

        String dbFileName = DATABASE_INIT_XML;
        if (DatabaseInit.class.getResource(".") != null) {
            dbFileName = DatabaseInit.class.getResource(".").getFile()
                    + ROOT_PATH + DATABASE_INIT_XML;
        }

        String dbDriver = null;
        String dbURL = null;
        String dbUser = null;
        String dbPwd = null;

        boolean silent = false;
        for (int i = 0; i < args.length; i++) {
            if ("-s".equals(args[i])) {
                silent = true;
            }
            if ("-f".equals(args[i])) {
                dbFileName = args[i + 1];
            }
            if ("-dbDriver".equals(args[i])) {
                dbDriver = args[i + 1];
            }
            if ("-dbURL".equals(args[i])) {
                dbURL = args[i + 1];
            }
            if ("-dbUser".equals(args[i])) {
                dbUser = args[i + 1];
            }
            if ("-dbPwd".equals(args[i])) {
                dbPwd = args[i + 1];
            }
        }

        if (dbDriver == null) {
            System.out.println("Missing setting 'dbDriver'");
        }
        if (dbURL == null) {
            System.out.println("Missing setting 'dbURL'");
        }
        if (dbUser == null) {
            System.out.println("Missing setting 'dbUser'");
        }
        if (dbPwd == null) {
            System.out.println("Missing setting 'dbPwd'");
        }

        if (silent
                || JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null,
                        "Do you really want to initialize the database:\n"
                                + dbURL + "\nAll existing data will be LOST!",
                        "Confirmation", JOptionPane.OK_CANCEL_OPTION)) {

            DatabaseTaskHandler.init(dbDriver, dbURL, dbUser, dbPwd);

            // load
            DatabaseTaskHandler.insertData(dbFileName);

            System.out.println("File " + dbFileName
                    + " inserted into Database '" + dbURL + "'.");
        }
    }
}
