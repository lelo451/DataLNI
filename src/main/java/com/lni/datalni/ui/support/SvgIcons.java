package com.lni.datalni.ui.support;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.ViewBox;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Renders an SVG classpath resource to a JavaFX {@link Image} at a requested pixel size
 * (JavaFX has no native SVG support). Uses jsvg's Java2D renderer, then copies the pixels
 * into a {@link WritableImage} so no {@code javafx-swing} dependency is needed.
 */
public final class SvgIcons {

    private SvgIcons() {
    }

    /** @return the rendered square image, or {@code null} if the resource is missing/invalid. */
    public static Image render(String resourcePath, int size) {
        URL url = SvgIcons.class.getResource(resourcePath);
        if (url == null) {
            return null;
        }
        try {
            SVGDocument document = new SVGLoader().load(url);
            if (document == null) {
                return null;
            }
            BufferedImage buffer = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = buffer.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            document.render((Component) null, g, new ViewBox(0, 0, size, size));
            g.dispose();
            return toFxImage(buffer);
        } catch (Exception e) {
            return null;
        }
    }

    private static Image toFxImage(BufferedImage buffer) {
        int w = buffer.getWidth();
        int h = buffer.getHeight();
        int[] argb = buffer.getRGB(0, 0, w, h, null, 0, w);
        WritableImage image = new WritableImage(w, h);
        image.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), argb, 0, w);
        return image;
    }
}
