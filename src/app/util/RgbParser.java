package app.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RgbParser {
    public static Image readImageRGB(int width, int height, File file)
    {
        WritableImage img = new WritableImage(width, height);
        PixelWriter pixelWriter = img.getPixelWriter();
        try
        {
            int frameLength = width*height*3;

            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    int pix = ImageTransform.buildRGB(r, g, b);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    pixelWriter.setArgb(x,y,pix);
                    ind++;
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return img;
    }
}
