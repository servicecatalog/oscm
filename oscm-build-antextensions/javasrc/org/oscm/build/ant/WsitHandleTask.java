/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 4,2015                                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author qiu
 * 
 */
public class WsitHandleTask extends Task {

    private String dirName;
    private String fileName;
    private final String xmlContent = "<?xml version='1.0' encoding='UTF-8'?>";

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    @Override
    public void execute() throws BuildException {
        if (StringUtils.isEmpty(dirName)) {
            throw new BuildException("dirname is mandatory");
        }
        if (StringUtils.isEmpty(fileName)) {
            throw new BuildException("filename is mandatory");
        }
        File file = new File(dirName + File.separator + fileName);
        if (!file.exists()) {
            throw new BuildException("wsit file does not exist");
        }
        try {
            String content = convertStreamToString(file);
            content = xmlContent + content;
            FileWriter fw = new FileWriter(file);
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private String convertStreamToString(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = -1;
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        is.close();
        return baos.toString();
    }
}
