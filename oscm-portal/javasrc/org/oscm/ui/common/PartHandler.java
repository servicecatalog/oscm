/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2017-01-26                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.Part;

/**
 * 
 * @author KowalczykA
 * 
 *         Utility class for handling JSF 2.2 file upload (related to
 *         javax.servlet.http.Part)
 */
public class PartHandler {

    public static byte[] getBuffer(Part file) throws IOException {

        InputStream fileInStream = file.getInputStream();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        byte[] bytes = new byte[512];

        int readBytes;
        while ((readBytes = fileInStream.read(bytes)) > 0) {
            outputStream.write(bytes, 0, readBytes);
        }

        return outputStream.toByteArray();
    }
    
    public static boolean isEmpty(Part file) {
        
        if(file==null){
            return true;
        }
        
        return !(file.getSize()>0);
    }
}
