/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;
import java.io.FileInputStream;
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
public class PropertiesCompareTask extends Task {

    private final Union compareTo = new Union();
    private File reference;
    private String targetPath;

    /**
     * Reference property File.
     */
    public void setReference(File reference) {
        this.reference = reference;
    }

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
        Properties a;
        try {
            a = load(new FileInputStream(reference));
        } catch (IOException e) {
            throw new BuildException("Can't open file " + reference, e);
        }
        final Iterator<?> resourceIterator = compareTo.iterator();
        while (resourceIterator.hasNext()) {
            final Resource resource = (Resource) resourceIterator.next();
            final String name = resource.getName();
            Properties b;
            try {
                b = load(resource.getInputStream());
            } catch (IOException e) {
                throw new BuildException("Can't open file " + name, e);
            }
            compare(name, a, b);
        }
    }

    private void compare(String filename, Properties a, Properties b) {
        final PropertiesComparator comp = new PropertiesComparator(a, b);

        boolean differencesExist = comp.getMissingKeys().size()
                + comp.getAdditionalKeys().size()
                + comp.getDifferentVariables().size() > 0;

        PrintStream ps = null;

        // extract possibly contained locale
        String path = reference.getName();
        if (path.contains("_")) {
            path = path.replace(
                    path.substring(path.indexOf("_"), path.indexOf(".")), "");
        }

        // build absolute path
        path = targetPath + File.separator + "diff_" + path + ".log";
        File myFile = new File(path);

        try {
            if (differencesExist && myFile.createNewFile()) {
                log("There are differences between property files. For more information, see /properties/"
                        + myFile.getAbsoluteFile().getName(), Project.MSG_WARN);
            }
            FileOutputStream fo = new FileOutputStream(myFile, true);
            ps = new PrintStream(fo);
            warn(filename, comp.getMissingKeys(), "Missing Key", ps);
            warn(filename, comp.getAdditionalKeys(), "Additional Key", ps);
            warn(filename, comp.getDifferentVariables(), "Different Variables",
                    ps);
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
            if (in != null) {
                in.close();
            }
        }
    }

}
