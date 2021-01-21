package fxmlController;


import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.text.Text;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.tensorflow.Tensor;
import utils.ImageDescription;
import utils.TensorFlowUtils;
import utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;


public class GridImageController {
    @FXML
    private Text textProbability;
    @FXML
    private Text textObject;
    @FXML
    private Text textIndex;
    @FXML
    private ImageView imageView;

    private Frame frame;
    private ApplicationController owner;
    private final Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();

    public void setOwner(ApplicationController owner) {
        this.owner = owner;
    }

    public void setDescription(ImageDescription imageDescription) {
        this.textObject.setText(imageDescription != null ? "Object: " + imageDescription.getLabel() : "Object: ");
        this.textIndex.setText(imageDescription != null ? "Index: " + imageDescription.getIndex() : "Index: ");
        this.textProbability.setText(imageDescription != null ? "Probability: " + Utils.round(imageDescription.getProbability() * 100, 2) + "%" : "Probability: ");
        String separator = this.owner.getOS().contains("win") ? "/" : "//";
        this.imageView.setImage(imageDescription != null ? new Image("file:" + separator + imageDescription.getPath()) : null);
    }

    public ImageView getImageView() {
        return this.imageView;
    }

    public void setCam() throws FrameGrabber.Exception {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(2);
        grabber.start();

        Executors.newSingleThreadExecutor().execute(() -> {

            while (!this.owner.getDisabledWebCam()) {
                try {
                    this.frame = grabber.grabFrame();
                    setFrameToImageView(this.frame);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                grabber.stop();
                grabber.close();

            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            this.owner.resetImageDescription();
        });
        try {
            this.setDescriptionCam();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setFrameToImageView(Frame frame) {
        this.getImageView().setImage(frameToImage(frame));
    }

    private WritableImage frameToImage(Frame frame) {
        BufferedImage bufferedImage = java2DFrameConverter.getBufferedImage(frame);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    private void setDescriptionCam() throws IOException {
        Executors.newSingleThreadExecutor().execute(() -> {
            TensorFlowUtils tensorFlowUtils = new TensorFlowUtils();
            while (!this.owner.getDisabledWebCam()) {
                try {
                    Thread.sleep(this.owner.getSpinnerTime());
                    BufferedImage bufferedImage = java2DFrameConverter.getBufferedImage(frame);
                    System.out.println("hello1");
                    byte[] bytes = toByteArray(bufferedImage, "jpg");
                    Tensor<Float> tensor2 =  tensorFlowUtils.byteBufferToTensor(bytes);
                    System.out.println("hello2");
                    if (tensor2 == null) {
                        System.out.println("hello3");
                        continue;
                    }
                    Tensor<Float> tensor = tensorFlowUtils.executeModelFromByteArray(
                            this.owner.graphDef,
                            tensor2
                    );
                    System.out.println("hello4");
                    this.owner.setImageDescription(tensorFlowUtils.getDescription(null, tensor, this.owner.allLabels));
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static byte[] toByteArray(BufferedImage bufferedImage, String format)
            throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, format, baos);
        return baos.toByteArray();
    }
}
