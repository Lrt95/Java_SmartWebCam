package imageFilterManager;

public class imageFilterManager {

    private int red;
    private int green;
    private int blue;
    private int alpha;

    private boolean isFilterColorApply;
    private boolean isFilterBorderApply;
    private boolean isFilterPictureApply;

    public imageFilterManager(int red, int green, int blue, int alpha) {
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
}
