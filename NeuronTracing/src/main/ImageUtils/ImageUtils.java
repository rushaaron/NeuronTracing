package main.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageUtils {

    private ImageUtils(){
        //Do nothing
    }

    //get the red rgb value
    public static int getRed(int rgb) {
        Color c = new Color(rgb,true);
        int red = c.getRed();
        return red;
    }

    public static boolean containsPink(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++){
            for (int x = 0; x < image.getWidth(); x++){
                if (isPink(image.getRGB(x, y))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isPink(int rgb) {
        Color c = new Color(rgb,true);
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();

        if (red > 200 && green < 100 && blue > 200)
            return true;
        return false;
    }


    //setting a file as white or to mimic the one passed
    public static void setImageToWhite(BufferedImage img, BufferedImage input, boolean printWhite) {
        Color white = new Color(255,255,255);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (printWhite)
                    img.setRGB(x, y, white.getRGB());
                else
                    img.setRGB(x,y,input.getRGB(x, y));
            }
        }
    }




}
