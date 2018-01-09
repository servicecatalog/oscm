/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Sep 16, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 16, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oscm.webtest.base.PropertiesReader;
import org.oscm.internal.intf.ServiceProvisioningService;

/**
 * Custom ANT task importing technical services definitions using the WS-API.
 * 
 * @author Dirk Bernsau
 * 
 */
public class TechServiceImportTask extends WebtestTask {

    private String fileImport;

    public void setFile(String value) {
        fileImport = value;
    }

    @Override
    public void executeInternal() throws Exception {
        if (fileImport == null || fileImport.trim().length() == 0) {
            throwBuildException("No definition file specified - use the 'file' attribute to specify the file to be imported");

        }
        File baseDir = getProject().getBaseDir();
        File file = new File(baseDir, fileImport);
        if (!file.exists()) {
            throw new WebtestTaskException("Definition file does not exist: "
                    + file.getAbsolutePath());
        }
        String httpToken = "@techService.http.baseurl@";
        String httpsToken = "@techService.https.baseurl@";

        PropertiesReader reader = new PropertiesReader();
        Properties props = reader.load();
        String httpUrl = props.getProperty("example.http.url");
        String httpsUrl = props.getProperty("example.https.url");

        log("------------------------------------------------", 2);
        log("Imported TechServices from file:          " + fileImport, 2);
        log("ANT property (example.http.url):  " + httpUrl, 2);
        log("ANT property (example.https.url): " + httpsUrl, 2);
        log("------------------------------------------------", 2);

        ByteArrayOutputStream outputStream = null;
        BufferedReader in = null;
        try {
            outputStream = new ByteArrayOutputStream(1024);
            in = new BufferedReader(new FileReader(file));
            byte[] bytes = new byte[512];

            String line = null;
            while ((line = in.readLine()) != null) {
                line = replaceToken(line, httpToken, httpUrl);
                line = replaceToken(line, httpsToken, httpsUrl);
                line = line + "\n";
                System.out.println(line);
                if (line.contains("baseUrl=")) {
                    Matcher match = Pattern.compile("baseUrl=\".+?\"").matcher(
                            line);
                    if (match.find()) {
                        String s = match.group();
                        getProject().setProperty("technicalBaseUrl",
                                s.split("\"")[1]);
                    }
                }
                bytes = line.getBytes("UTF-8");
                outputStream.write(bytes, 0, bytes.length);
            }

            ServiceProvisioningService spsSvc = getServiceInterface(ServiceProvisioningService.class);
            spsSvc.importTechnicalServices(outputStream.toByteArray());
        } catch (FileNotFoundException e) {
            throw new WebtestTaskException("Definition file does not exist: "
                    + file.getAbsolutePath());
        } catch (IOException e) {
            throw new WebtestTaskException("Failed to load file: "
                    + file.getAbsolutePath());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private String replaceToken(String line, String token, String replacement) {
        return line.replaceAll(token, replacement);
    }
}
