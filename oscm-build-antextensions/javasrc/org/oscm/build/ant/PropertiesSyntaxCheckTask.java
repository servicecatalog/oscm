/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Union;

/**
 * Task to compare a properties files against a set of properties file.
 */
public class PropertiesSyntaxCheckTask extends Task {

    private final Union compareTo = new Union();
    private String targetPath;

    /**
     * The target for the output file.
     */
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    /**
     * This task accepts any number of files to compare to.
     */
    public void addConfigured(final ResourceCollection resources) {
        compareTo.add(resources);
    }

    @Override
    public void execute() throws BuildException {
        final Iterator<?> resourceIterator = compareTo.iterator();
        while (resourceIterator.hasNext()) {
            final Resource resource = (Resource) resourceIterator.next();
            checkSyntax(resource);
        }
    }

    private void checkSyntax(Resource r) {
        // extract possibly contained locale
        String path = r.getName();
        if (path.contains("_")) {
            path = path.replace(
                    path.substring(path.indexOf("_"), path.indexOf(".")), "");
        }

        // build absolute path
        path = targetPath + File.separator + "syntax_" + path + ".log";
        File myFile = new File(path);

        Properties a;
        try {
            a = load(r.getInputStream());
        } catch (IOException e) {
            throw new BuildException("Can't open file " + r.getName(), e);
        }

        PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);

        boolean errorExist = false;
        if (checker.getSyntaxSingleQuotesErrorKeys().size() > 0) {
            errorExist = true;
        }

        PrintStream ps = null;
        try {
            if (errorExist && myFile.createNewFile()) {
                log("There are syntax errors in the property files. For more information, see /properties/"
                        + myFile.getAbsoluteFile().getName(), Project.MSG_WARN);
            }
            FileOutputStream fo = new FileOutputStream(myFile, true);
            ps = new PrintStream(fo);
            warn(r.getName(), checker.getSyntaxSingleQuotesErrorKeys(),
                    "Syntax problem - Single quotes around parameter", ps);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }

    }

    private void warn(String filename, Set<String> keys, String problem,
            PrintStream ps) {
        for (final String key : keys) {
            final String msg = String.format("%s[%s]: %s", filename, key,
                    problem);
            ps.println(msg);
        }
    }

    private static Properties load(InputStream in) throws IOException {
        try {
            final Properties p = new Properties();
            p.load(in);
            return p;
        } finally {
            in.close();
        }
    }

}
