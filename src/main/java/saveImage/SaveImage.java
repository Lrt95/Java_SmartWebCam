package saveImage;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utils.ImageDescription;

public class SaveImage {

    private final int minPercentage;
    private final String objectsName;
    private final String filePath;

    /**
     * SaveImage constructor
     * @param minPercentage The minimum percentage expected to record a picture
     * @param objectsName The name of the picture
     * @param filePath The path to save the picture
     */
    public SaveImage(int minPercentage, String objectsName, String filePath) {
        this.minPercentage = minPercentage;
        this.objectsName = objectsName;
        this.filePath = filePath;
    }

    /**
     * Write the picture of the information given
     * @param imageDescription The information from a picture
     */
    public void save(ImageDescription imageDescription) {
        List<String> listObjectsName = new ArrayList<>(Arrays.asList(this.objectsName.split(" ")));

        int percentage = Math.round(imageDescription.getProbability() * 100);
        if (percentage >= this.minPercentage  &&
            listObjectsName.contains(imageDescription.getLabel())) {
            try {
                File newFile = new File(imageDescription.getPath());
                BufferedImage bufferedImage = ImageIO.read(newFile);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                LocalDateTime now = LocalDateTime.now();
                String fileName = dtf.format(now) + "_" + this.objectsName + "_" + percentage + ".jpg";
                ImageIO.write(bufferedImage, "jpg", new File(filePath + "/" + fileName));
            }
            catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
}
