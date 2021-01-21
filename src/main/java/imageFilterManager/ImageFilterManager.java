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

    public ImageFilterManager(int red, int green, int blue, int alpha) {
        this.isFilterColorApply = false;
        this.isFilterBorderApply = false;
        this.isFilterPictureApply = false;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
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

    public void applyFilters(BufferedImage bufferedImage) throws IOException {
        if (this.isFilterColorApply) {
            applyColorFilter(bufferedImage);
        }
        if (this.isFilterBorderApply) {
            applyBorderFilter(bufferedImage);
        }
        if (this.isFilterPictureApply){
            applyPictureFilter(bufferedImage);
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
        mergeBufferedImage(bufferedImage, bImage, (float)this.alpha / 255);
    }

    private void applyBorderFilter(BufferedImage bufferedImage) throws IOException {
        BufferedImage cadreBuffered = ImageIO.read(new File("src/main/resources/images/cadre.png"));
        mergeBufferedImage(bufferedImage, cadreBuffered, 1f);
    }

    private void applyPictureFilter(BufferedImage bufferedImage) {

    }

    private void mergeBufferedImage(BufferedImage bufferedImage, BufferedImage cadreBuffered, float alpha) {
        BufferedImage cadreResize = resize(cadreBuffered, bufferedImage.getWidth(), bufferedImage.getHeight());
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
        int x = (bufferedImage.getWidth() - cadreResize.getWidth()) / 2;
        int y = (bufferedImage.getHeight() - cadreResize.getHeight()) / 2;

        g2d.drawImage(cadreResize, x, y, null);
        g2d.dispose();
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        java.awt.Image tmp = img.getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}
