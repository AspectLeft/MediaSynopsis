package app.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class ImageTransform {
    private static final int[][] DIRS = {
            {-1, -1}, {0, -1}, {1, -1},
            {-1, 0}, {0, 0}, {1, 0},
            {-1, 1}, {0, 1}, {1, 1}
    };

    private int W0, H0;
    private Image img0;

    public ImageTransform(Image image) {
        this.img0 = image;
        this.W0 = (int) img0.getWidth();
        this.H0 = (int) img0.getHeight();
    }

    public Image scale(double s, boolean a) {
        int w = (int) (W0 * s);
        int h = (int) (H0 * s);
        WritableImage img = new WritableImage(w, h);
        PixelWriter pixelWriter = img.getPixelWriter();
        PixelReader pixelReader = img0.getPixelReader();
        int x0, y0;
        int dx, dy;
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                dx = x - w / 2;
                dy = y - h / 2;
                x0 = W0 / 2 + (int) (dx / s);
                y0 = H0 / 2 + (int) (dy / s);
                if (!inBound(img0, x0, y0)) continue;
                if (!a) {
                    pixelWriter.setArgb(x, y, pixelReader.getArgb(x0, y0));
                }
                else {
                    pixelWriter.setArgb(x, y, lowPassFilter(img0, x0, y0));
                }
            }
        }
        return img;
    }

    public static int buildRGB(int r, int g, int b) {
        return 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    private static int[] breakRGB(int rgb) {
        return new int[]{(rgb & 0xff0000) >> 16, (rgb & 0xff00) >> 8, rgb & 0xff};
    }

    private boolean inBound(Image image, int x, int y) {
        return 0 <= x && x < image.getWidth() && 0 <= y && y < image.getHeight();
    }

    private int lowPassFilter(Image image, int x0, int y0) {
        int r = 0, g = 0, b = 0;
        int x, y;
        int n = 0;
        for (int[] dir: DIRS) {
            x = x0 + dir[0];
            y = y0 + dir[1];
            if (!inBound(image, x, y)) continue;
            n++;
            int[] rgb = breakRGB(image.getPixelReader().getArgb(x, y));
            r += rgb[0];
            g += rgb[1];
            b += rgb[2];
        }
        if (n == 0) return 0;
        r /= n;
        g /= n;
        b /= n;
        return buildRGB(r, g, b);
    }

}
