import org.tensorflow.Tensor;
import utils.TFUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class Main {

    public static void main(String[] argv) throws IOException {

        TFUtils utils = new TFUtils();

        Tensor<?> tensor = utils.executeModelFromByteArray(
                readFile("inception5h/tensorflow_inception_graph.pb"),
                utils.byteBufferToTensor(readPicture("inception5h/tensorPics/jack.jpg"))
        );

        System.out.println("end");
    }

    private static byte[] readPicture (String path) throws IOException {
        // open image
        File imgPath = new File(path);
        BufferedImage bufferedImage = ImageIO.read(imgPath);

        // get DataBufferBytes from Raster
        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte data   = (DataBufferByte)raster.getDataBuffer();

        return ( data.getData() );
    }

    private static byte[] readFile(String path) throws IOException {
        byte[] c = Files.readAllBytes(new File(path).toPath());
        return c;
    }
}
