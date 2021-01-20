package saveImage;

import utils.ImageDescription;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SaveImage {
    Integer percentage;
    String objectsName;
    String filePath;

    public SaveImage(Integer percentage, String objectsName, String filePath) {
        this.percentage = percentage;
        this.objectsName = objectsName;
        this.filePath = filePath;
    }

    public void save(ImageDescription imageDescription) {
        List<String> listObjectsName = new ArrayList<>(Arrays.asList(this.objectsName.split(" ")));

        if(this.percentage <= imageDescription.getProbability() * 100 && listObjectsName.contains(imageDescription.getLabel())){
            try {
                File newfile = new File(imageDescription.getPath());
                BufferedImage bufferedImage = null;
                bufferedImage = ImageIO.read(newfile);
                ImageIO.write(bufferedImage, "jpg", new File(filePath + "/" + imageDescription.getLabel() + ".jpg"));
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
}
