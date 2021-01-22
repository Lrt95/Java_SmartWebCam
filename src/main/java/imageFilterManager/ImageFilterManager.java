package imageFilterManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageFilterManager {

    private int red;
    private int green;
    private int blue;
    private int alpha;

    private boolean isFilterColorApply;
    private boolean isFilterBorderApply;
    private boolean isFilterPictureApply;

    private String pathPicture;
    private String pathBorder;
    private int xPicture;
    private int yPicture;

    public static String PATH_PICTURE = "src/main/resources/images/tampon.png";
    public static String PATH_BORDER = "src/main/resources/images/border.png";

    public ImageFilterManager(int red, int green, int blue, int alpha) {
        this.isFilterColorApply = false;
        this.isFilterBorderApply = false;
        this.isFilterPictureApply = false;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.pathPicture = PATH_PICTURE;
        this.pathBorder = PATH_BORDER;
    }

    private void setRed(int red) {
        this.red = bounds(red);
    }

    private void setGreen(int green) {
        this.green = bounds(green);
    }

    private void setBlue(int blue) {
        this.blue = bounds(blue);
    }

    private void setAlpha(int alpha) {
        this.alpha = bounds(alpha);
    }

    private int bounds(int value) {
        if (value < 0) value = 0;
        if (value > 255) value = 255;
        return value;
    }

    public void setIsFilterColorApply(boolean value) {
        this.isFilterColorApply = value;
    }

    public boolean getIsFilterColorApply() {
        return this.isFilterColorApply;
    }

    public void setIsFilterBorderApply(boolean value) {
        this.isFilterBorderApply = value;
    }

    public boolean getIsFilterBorderApply() {
        return this.isFilterBorderApply;
    }

    public void setIsFilterPictureApply(boolean value) {
        this.isFilterPictureApply = value;
    }

    public boolean getIsFilterPictureApply() {
        return this.isFilterPictureApply;
    }

    public void setFromString(String value) {
        String[] values = value.split(",");
        if (values.length == 4) {
            try {
                setRed(Integer.parseInt(values[0]));
            } catch (NumberFormatException ignored) {}
            try {
                setGreen(Integer.parseInt(values[1]));
            } catch (NumberFormatException ignored) {}
            try {
                setBlue(Integer.parseInt(values[2]));
            } catch (NumberFormatException ignored) {}
            try {
                setAlpha(Integer.parseInt(values[3]));
            } catch (NumberFormatException ignored) {}
        }
    }

    public String getColor() {
        return this.red + "," + this.green + "," + this.blue + "," + this.alpha;
    }

    public String getPathPicture() {
        return pathPicture;
    }

    public void setPathPicture(String pathPicture) {
        this.pathPicture = pathPicture;
    }

    public String getPathBorder() {
        return this.pathBorder;
    }

    public void setPathBorder(String pathBorder) {
        this.pathBorder = pathBorder;
    }

    public int getXPicture() {
        return xPicture;
    }

    public void setXPicture(int xPicture) {
        this.xPicture = xPicture;
    }

    public int getYPicture() {
        return yPicture;
    }

    public void setYPicture(int yPicture) {
        this.yPicture = yPicture;
    }

    public void setXPictureFromString(String xPicture) {
        try {
            setXPicture(Integer.parseInt(xPicture));
        } catch (NumberFormatException ignored) {}
    }

    public void setYPictureFromString(String yPicture) {
        try {
            setYPicture(Integer.parseInt(yPicture));
        } catch (NumberFormatException ignored) {}
    }

    public void applyFilters(BufferedImage bufferedImage) throws IOException {
        if (this.isFilterColorApply) {
            applyColorFilter(bufferedImage);
        }
        if (this.isFilterPictureApply){
            applyPictureFilter(bufferedImage);
        }
        if (this.isFilterBorderApply) {
            applyBorderFilter(bufferedImage);
        }
    }

    private void applyColorFilter(BufferedImage bufferedImage) {
        BufferedImage bImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Color color = new Color(this.red, this.green, this.blue);
        for (int i = 0; i < bImage.getHeight(); i++) {
            for (int j = 0; j < bImage.getWidth(); j++) {
                bImage.setRGB(j, i, color.getRGB());
            }
        }
        mergeBufferedImage(bufferedImage, bImage, (float)this.alpha / 255, 0, 0, true);
    }

    private void applyBorderFilter(BufferedImage bufferedImage) throws IOException {
        BufferedImage cadreBuffered = ImageIO.read(new File(this.getPathBorder()));
        mergeBufferedImage(bufferedImage, cadreBuffered, 1f, 0, 0, true);
    }

    private void applyPictureFilter(BufferedImage bufferedImage) throws IOException {
        BufferedImage tamponBuffered = resize(ImageIO.read(new File(this.getPathPicture())), 75, 75);
        mergeBufferedImage(bufferedImage, tamponBuffered, 1f, this.getXPicture(), this.getYPicture(), false);

    }

    private void mergeBufferedImage(BufferedImage firstBuffer, BufferedImage secondBuffer, float alpha, int x, int y, boolean isResizing) {
        if (isResizing) {
            secondBuffer = resize(secondBuffer, firstBuffer.getWidth(), firstBuffer.getHeight());
        }
        Graphics2D g2d = firstBuffer.createGraphics();
        g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2d.drawImage(secondBuffer, x, y, null);
        g2d.dispose();
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        java.awt.Image tmp = img.getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
        BufferedImage dImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dImg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dImg;
    }
}
