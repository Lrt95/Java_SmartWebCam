package imageFilterManager;

public class imageFilterManager {

    private int red;
    private int green;
    private int blue;
    private int alpha;

    public imageFilterManager(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public boolean setFromString(String value) {
        String[] values = value.split(",");
        if (values.length != 4) {
            return false;
        }
        else {
            System.out.println("r: " + values[0]);
            System.out.println("g: " + values[1]);
            System.out.println("b: " + values[2]);
            System.out.println("a: " + values[3]);
        }
        return true;
    }
}
