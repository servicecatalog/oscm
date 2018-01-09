/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 06.06.2011                                                      
 *                                                                              
 *  Completion Time: 06.06.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.dataservice.bean;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.test.db.ITestDB;
import org.oscm.test.ejb.TestDataSources;

/**
 * Test compares the hibernate_sequence table and checks if the content is
 * consistent with the domain objects.
 * 
 * @author cheld
 * 
 */
public class HibernateSequenceIT {

    private static final ITestDB TESTDB = TestDataSources
            .get("oscm-domainobjects");

    private static final DataSource ds = TESTDB.getDataSource();

    @BeforeClass
    public static void setup() throws Exception {
        TESTDB.purgeSchema();
        TESTDB.loadSchema();
    }

    /**
     * Test compares the hibernate domain objects with the hibernate_sequence
     * table and checks if the table is missing some rows.
     * 
     * @throws Exception
     */
    @Test
    public void testMissingSequenceNames() throws Exception {
        Set<String> sequenceNames = executeQuery();
        Set<String> classNames = getClassNames();

        Set<String> missingSequences = new HashSet<String>();
        missingSequences.addAll(classNames);
        missingSequences.removeAll(sequenceNames);

        Assert.assertArrayEquals(
                "There are missing entries in the hibernate_sequence table",
                Collections.EMPTY_SET.toArray(), missingSequences.toArray());
    }

    /**
     * Test compares the hibernate domain objects with the hibernate_sequence
     * table and checks if the table has additional rows that can be removed.
     * 
     * @throws Exception
     */
    @Test
    public void testObsoleteSequenceNames() throws Exception {
        Set<String> sequenceNames = executeQuery();
        Set<String> classNames = getClassNames();

        Set<String> obsoleteSequences = new HashSet<String>();
        obsoleteSequences.addAll(sequenceNames);
        obsoleteSequences.removeAll(classNames);

        Assert.assertArrayEquals(
                "There are obsolete entries in the hibernate_sequence table: "
                        + obsoleteSequences, Collections.EMPTY_SET.toArray(),
                obsoleteSequences.toArray());
    }

    /**
     * Read the sequence names from the database.
     */
    private static Set<String> executeQuery() throws Exception {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            String sqlQuery = createSqlQuery();
            rs = stmt.executeQuery(sqlQuery);
            return read(rs);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    private static String createSqlQuery() {
        StringBuffer sb = new StringBuffer();
        sb.append("select sequence_name from hibernate_sequences");
        return sb.toString();
    }

    /**
     * Read the sequence names from the result set
     */
    private static Set<String> read(ResultSet rs) throws Exception {
        Set<String> names = new HashSet<String>();
        while (rs.next()) {
            String sequenceName = rs.getString(1);
            names.add(sequenceName);
        }
        return names;
    }

    private static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {
                ignore.printStackTrace();
            }
        }
    }

    private static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) {
                ignore.printStackTrace();
            }
        }
    }

    private static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) {
                ignore.printStackTrace();
            }
        }
    }

    /**
     * Returns a list of all domain objects that are mapped by hibernate
     */
    public static Set<String> getClassNames() throws Exception {
        List<Class<?>> classes = getClasses("org.oscm.domobjects");
        classes.addAll(getClasses("org.oscm.i18nservice.bean"));
        return filterEntities(classes);
    }

    /**
     * Filter all classes that do not have the annotation entity
     */
    static Set<String> filterEntities(List<Class<?>> classes) {
        Set<String> names = new HashSet<String>();
        for (Class<?> clazz : classes) {
            if (clazz.getAnnotation(Entity.class) != null) {
                names.add(clazz.getSimpleName());
            }
        }
        return names;
    }

    /**
     * Scans all classes accessible from the context class loader which belong
     * to the given package and subpackages.
     * 
     * @param packageName
     *            The base package
     * @return The classes
     */
    private static List<Class<?>> getClasses(String packageName)
            throws Exception {
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName)
            throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file,
                        packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName
                        + '.'
                        + file.getName().substring(0,
                                file.getName().length() - 6)));
            }
        }
        return classes;
    }

}
