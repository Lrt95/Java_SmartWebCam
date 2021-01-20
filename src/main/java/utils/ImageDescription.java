package utils;

public class ImageDescription {

    private final int index;
    private final float probability;
    private final String label;

    public ImageDescription(int index, float probability, String label) {
        this.index = index;
        this.probability = probability;
        this.label = label;
    }

    public int getIndex() {
        return this.index;
    }

    public float getProbability() {
        return this.probability;
    }

    public String getLabel() {
        return label;
    }
}

