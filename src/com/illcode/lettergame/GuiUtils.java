package com.illcode.lettergame;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class GuiUtils
{
    static GraphicsEnvironment graphicsEnvironment;
    static GraphicsConfiguration graphicsConfiguration;
    static Toolkit toolkit;
    static Font letterFont;

    private static RenderingHints renderingHints;

    static void initGraphics() {
        graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        graphicsConfiguration = graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration();
        toolkit = Toolkit.getDefaultToolkit();
    }

    static void syncToolkit() {
        toolkit.sync();
    }

    /** Creates a BufferedImage compatible with our default screen device, with BITMASK transparency.
     *  This means that each pixel of the image will be either completely opaque (alpha 255) or
     *  completely translucent (alpha 0). */
    public static BufferedImage createBitmaskImage(final int width, final int height) {
        return graphicsConfiguration.createCompatibleImage(width, height, Transparency.BITMASK);
    }

    /** Creates a BufferedImage compatible with our default screen device, with OPAQUE transparency. */
    public static BufferedImage createOpaqueImage(final int width, final int height) {
        return graphicsConfiguration.createCompatibleImage(width, height, Transparency.OPAQUE);
    }

    /** Load an opaque image from a given Path */
    public static BufferedImage loadOpaqueImage(Path p) {
        BufferedImage image;
        try {
            BufferedImage bi = ImageIO.read(p.toFile());
            image = createOpaqueImage(bi.getWidth(), bi.getHeight());
            Graphics g = image.getGraphics();
            g.drawImage(bi, 0, 0, null);
            g.dispose();
            bi.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            image = null;
        }
        return image;
    }

    /**
     * Load a bitmask image from a given Path, optionally making a specified color transparent.
     * @param path path from which to load the image
     * @param transparentColor the 0x00RRGGBB color to make transparent. If equal to -1, no
     *          color transparency filtering will be performed.
     */
    public static BufferedImage loadBitmaskImage(Path path, int transparentColor) {
        BufferedImage image = null;
        try {
            BufferedImage bi = ImageIO.read(path.toFile());
            int w = bi.getWidth();
            int h = bi.getHeight();
            if (transparentColor != -1) {
                int[] rgb = bi.getRGB(0, 0, w, h, null, 0, w);
                for (int i = 0; i < rgb.length; i++) {
                    if ((rgb[i] & 0x00FFFFFF) == transparentColor)
                        rgb[i] = 0;  // fully transparent black
                }
                bi.setRGB(0, 0, w, h, rgb, 0, w);
            }
            image = GuiUtils.createBitmaskImage(w, h);
            Graphics g = image.getGraphics();
            g.drawImage(bi, 0, 0, null);
            g.dispose();
            bi.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            image = null;
        }
        return image;
    }

    /**
     * Returns a scaled version of a buffered image.
     * @param img image to scale
     * @param scale the factor {@code (> 1)} by which to scale the image
     * @return scaled image
     */
    public static BufferedImage getScaledImage(BufferedImage img, int scale) {
        if (scale <= 1)
            return img;
        return getSubImage(img, 0, 0, img.getWidth(), img.getHeight(), scale);
    }

    /**
     * Returns a new image generated from a sub-rectangle of the given source image.
     * @param img source image
     * @param sx1 the x coordinate of the first corner of the source rectangle.
     * @param sy1 the y coordinate of the first corner of the source rectangle.
     * @param sx2 the x coordinate of the second corner of the source rectangle.
     * @param sy2 the y coordinate of the second corner of the source rectangle.
     * @param scale scale factor {@code (>= 1)} by which to enlarge the resulting image
     * @return new image
     */
    public static BufferedImage getSubImage(BufferedImage img, int sx1, int sy1, int sx2, int sy2, int scale) {
        if (scale < 1)
            scale = 1;
        int dw = (sx2 - sx1) * scale;
        int dh = (sy2 - sy1) * scale;
        BufferedImage subImage = img.getTransparency() == Transparency.OPAQUE
            ? createOpaqueImage(dw, dh) : createBitmaskImage(dw, dh);
        Graphics2D g2d = subImage.createGraphics();
        g2d.setRenderingHints(getRenderingHints());
        g2d.drawImage(img, 0, 0, dw, dh, sx1, sy1, sx2, sy2, null);
        g2d.dispose();
        return subImage;
    }

    static RenderingHints getRenderingHints() {
        if (renderingHints == null) {
            renderingHints = new RenderingHints(null);
            renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            renderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }
        return renderingHints;
    }

    static boolean registerGameFonts() {
        letterFont = Font.decode("Lucida Sans PLAIN 24");
        return true;
    }

}
