package saveImage;

import utils.ImageDescription;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SaveImage {
    Integer percentage;
    String objectsName;
    File file;

    public SaveImage(Integer percentage, String objectsName, File file) {
        this.percentage = percentage;
        this.objectsName = objectsName;
        this.file = file;
    }

    public void save(ImageDescription imageDescription, String imagePath) throws IOException {
        List<String> listObjectsName = new ArrayList<>();
        for(String objectName : this.objectsName.split(" ")){
            listObjectsName.add(objectName);
        }

        if(this.percentage <= imageDescription.getProbability() && listObjectsName.contains(imageDescription.getLabel())){
            try {
                FileWriter myWriter = new FileWriter("");
                myWriter.write("Files in Java might be tricky, but it is fun enough!");
                myWriter.close();
                System.out.println("Successfully wrote to the file.");
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
}
