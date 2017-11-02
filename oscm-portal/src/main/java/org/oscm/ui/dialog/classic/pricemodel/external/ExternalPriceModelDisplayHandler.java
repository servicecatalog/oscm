/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.pricemodel.external;

import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

public class ExternalPriceModelDisplayHandler {

    private FacesContext facesContext = FacesContext.getCurrentInstance();
    private byte[] content;
    private String contentType;
    private String filename;
    private ExternalPriceModelDisplayType displayType = ExternalPriceModelDisplayType.INLINE;

    private static final String CONTENT_TYPE_PDF = "application/pdf";

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public ExternalPriceModelDisplayType getDisplayType() {
        return displayType;
    }

    public void setDisplayType(ExternalPriceModelDisplayType displayType) {
        this.displayType = displayType;
    }

    public void display() throws IOException {

        if (CONTENT_TYPE_PDF.equals(this.contentType)) {
            displayPDF();
        } else {
            // TODO: manage unsupported types
        }

    }

    private void displayPDF() throws IOException {
        HttpServletResponse response = (HttpServletResponse) this.facesContext
                .getExternalContext().getResponse();

        response.reset();
        response.setContentType(this.contentType);
        response.setContentLength(this.content.length);

        response.addHeader("Content-Disposition",
                this.getDisplayType(this.displayType) + "; " + "filename="
                        + this.filename);
        response.addHeader("Accept-Ranges", "bytes");

        BufferedOutputStream outputStream = new BufferedOutputStream(
                response.getOutputStream());
        outputStream.write(this.content);
        outputStream.flush();
        outputStream.close();

        facesContext.responseComplete();
    }

    private String getDisplayType(ExternalPriceModelDisplayType type) {

        if (ExternalPriceModelDisplayType.INLINE.equals(type)) {
            return "inline";
        } else if (ExternalPriceModelDisplayType.SAVE_AS.equals(type)) {
            return "attachment";
        }
        return "inline";
    }

}
