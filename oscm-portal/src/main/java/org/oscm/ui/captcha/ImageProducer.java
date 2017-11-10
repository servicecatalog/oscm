/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Aug 5, 2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.captcha;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * @author pravi
 * 
 */
public class ImageProducer extends Canvas {
    private static final long serialVersionUID = -8431284951937009818L;

    /**
     * @param bimage
     */
    private static BufferedImage blurImage(final BufferedImage bimage) {
        float[] fs = new float[] { 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f,
                1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f };
        final Kernel kernel = new Kernel(3, 3, fs);
        final BufferedImageOp op = new ConvolveOp(kernel);
        return op.filter(bimage, null);
    }

    private final String message;

    private static final int START_X = 0;
    private static final int START_Y = 0;
    private static final int END_X = 180;
    private static final int END_Y = 60;

    public ImageProducer(final String message) {
        this.message = message;
        setForeground(Color.BLACK);
    }

    protected BufferedImage createImage(Color bgColor) {
        BufferedImage bufferedImage = new BufferedImage(END_X, END_Y,
                BufferedImage.TYPE_INT_RGB);
        // create graphics and graphics2d
        final Graphics graphics = bufferedImage.getGraphics();
        final Graphics2D g2d = (Graphics2D) graphics;

        // set the background color
        g2d.setBackground(bgColor == null ? Color.gray : bgColor);
        g2d.clearRect(START_X, START_Y, END_X, END_Y);
        // create a pattern for the background
        createPattern(g2d);
        // set the fonts and font rendering hints
        Font font = new Font("Helvetica", Font.ITALIC, 30);
        g2d.setFont(font);
        FontRenderContext frc = g2d.getFontRenderContext();
        g2d.translate(10, 24);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(3));

        // sets the foreground color
        g2d.setPaint(Color.DARK_GRAY);
        GlyphVector gv = font.createGlyphVector(frc, message);
        int numGlyphs = gv.getNumGlyphs();
        for (int ii = 0; ii < numGlyphs; ii++) {
            AffineTransform at;
            Point2D p = gv.getGlyphPosition(ii);
            at = AffineTransform.getTranslateInstance(p.getX(), p.getY());
            at.rotate(Math.PI / 8);
            Shape shape = gv.getGlyphOutline(ii);
            Shape sss = at.createTransformedShape(shape);
            g2d.fill(sss);
        }
        return blurImage(bufferedImage);
    }

    /**
     * @param g2d
     */
    private void createPattern(final Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        int variant = 5;
        // horizontal line
        for (int ii = 0; ii < END_Y; ii++) {
            g2d.drawLine(0, ii, END_X, ii);
            ii += variant;
        }
        // vertical line
        for (int ii = 0; ii < END_X; ii++) {
            g2d.drawLine(ii, 0, ii, END_Y);
            ii += variant;
        }
    }
}
