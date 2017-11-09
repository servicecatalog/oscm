/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Aug 28, 2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.servlet;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.captcha.Captcha;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ServiceAccess;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOImageResource;

/**
 * @author pravi
 * 
 */
public class ImageServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 3884283582812097285L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ImageServlet.class);

    private final static byte[] TRANSPARENT_PIXEL = new byte[] { 'G', 'I', 'F',
            '8', '9', 'a', 0x01, 0x00, 0x01, 0x00, (byte) 0x80, 0x01, 0x00,
            0x10, 0x3C, 0x63, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x21,
            (byte) 0xf9, 0x04, 0x01, 0x14, 0x00, 0x01, 0x00, 0x2c, 0x00, 0x00,
            0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x02, 0x02, 0x4c, 0x01,
            0x00, 0x3b };

    private Captcha createCaptcha(final String key, final Color bgColor) {
        final int wordLength = 4;
        final Captcha cap = new Captcha(wordLength);
        cap.createCaptcha(key, bgColor);
        return cap;
    }

    /**
     * Method to receive get requests from the web server (Passes them onto the
     * doPost method)
     * 
     * @param req
     *            The HttpServletRequest which contains the information
     *            submitted via get
     * @param res
     *            A response containing the required response data for this
     *            request
     **/
    @Override
    public void doGet(final HttpServletRequest req,
            final HttpServletResponse res) throws ServletException, IOException {
        doPost(req, res);
    }

    /**
     * Method to receive and process Post requests from the web server
     * 
     * @param req
     *            The HttpServletRequest which contains the information
     *            submitted via post
     * @param res
     *            A response containing the required response data for this
     *            request
     **/
    @Override
    public void doPost(final HttpServletRequest req,
            final HttpServletResponse res) throws ServletException, IOException {
        process(req, res);
    }

    /**
     * Writes the image with the requested id to the output stream of the
     * response
     * 
     * @param req
     *            the HttpServletRequest
     * @param res
     *            the HttpServletResponse
     * @throws IOException
     */
    private void process(final HttpServletRequest req,
            final HttpServletResponse res) throws IOException {
        try {
            final byte[] img;
            final String imgType;
            if (req.getParameter(Constants.REQ_PARAM_TYPE) == null) {
                imgType = "image/jpeg";
                img = createCaptcha(req);
            } else {
                final VOImageResource imageResource = retrieveVOImageResource(req);
                res.setHeader("Cache-Control", "max-age=1800");
                final Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, 30);
                res.setHeader("Expires", new SimpleDateFormat(
                        "EEE, d MMM yyyy HH:mm:ss z").format(cal.getTime()));
                if (imageResource == null) {
                    imgType = "image/gif";
                    img = TRANSPARENT_PIXEL;
                } else {
                    imgType = imageResource.getContentType();
                    img = imageResource.getBuffer();
                }
            }
            res.setContentType(imgType);
            res.getOutputStream().write(img);
        } catch (Throwable ex) {
            logger.logError(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.ERROR_IMAGE_FOR_SUPPLIER_NOT_FOUND);
        }
    }

    /**
     * @param req
     * @param imageTypeStr
     * @param session
     * @return
     */
    private VOImageResource retrieveVOImageResource(final HttpServletRequest req) {
        // determine the image type
        ImageType imageType = null;
        try {
            imageType = ImageType.valueOf(req
                    .getParameter(Constants.REQ_PARAM_TYPE));
        } catch (IllegalArgumentException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_WRONG_IMAGE_TYPE);
        }

        final HttpSession session = req.getSession(true);
        // load the image resource
        VOImageResource imageResource = null;
        if (imageType == ImageType.SERVICE_IMAGE) {
            String serviceId = req.getParameter(Constants.REQ_PARAM_SERVICE_ID);
            String serviceKey = req
                    .getParameter(Constants.REQ_PARAM_SERVICE_KEY);
            String supplierId = req
                    .getParameter(Constants.REQ_PARAM_SUPPLIER_ID);
            if (supplierId != null && serviceId != null) {
                ServiceProvisioningService service = getProvisioningService(session);
                try {
                    imageResource = service.loadImageForSupplier(serviceId,
                            supplierId);
                } catch (ObjectNotFoundException e) {
                    logger.logError(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.ERROR_IMAGE_FOR_SUPPLIER_NOT_FOUND);
                }
            } else if (serviceKey != null) {
                try {
                    Long lServiceKey = Long.valueOf(serviceKey);
                    ServiceProvisioningService service = getProvisioningService(session);
                    imageResource = service.loadImage(lServiceKey);
                } catch (NumberFormatException e) {
                    // ignore invalid key
                }
            }
        } else if (imageType == ImageType.ORGANIZATION_IMAGE) {
            String supplierId = req
                    .getParameter(Constants.REQ_PARAM_SUPPLIER_ID);

            if (supplierId != null && supplierId.trim().length() > 0) {
                imageResource = getAccountService(session)
                        .loadImageOfOrganization(
                                Long.valueOf(supplierId).longValue());

            }
        }
        return imageResource;
    }

    /**
     * @param req
     * @param res
     * @param session
     * @throws IOException
     */
    private byte[] createCaptcha(final HttpServletRequest req)
            throws IOException {
        Color color = Color.lightGray;
        final HttpSession session = req.getSession(true);
        Boolean result = (Boolean) session
                .getAttribute(Constants.CAPTCHA_INPUT_STATUS);
        if ("1".equals(req.getParameter("webtest"))) {
            result = Boolean.TRUE;
            session.setAttribute(Constants.CAPTCHA_KEY, "pp09");
        }
        final Captcha cap = Boolean.FALSE == result ? createCaptcha(null, color)
                : createCaptcha(
                        (String) session.getAttribute(Constants.CAPTCHA_KEY),
                        color);

        session.setAttribute(Constants.CAPTCHA_KEY, cap.getKey());
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(cap.getImage(), "jpeg", out);
        return out.toByteArray();
    }

    ServiceProvisioningService getProvisioningService(final HttpSession session) {
        ServiceProvisioningService service = ServiceAccess
                .getServiceAcccessFor(session).getService(
                        ServiceProvisioningService.class);
        return service;
    }

    AccountService getAccountService(final HttpSession session) {
        AccountService service = ServiceAccess.getServiceAcccessFor(session)
                .getService(AccountService.class);
        return service;
    }

}
