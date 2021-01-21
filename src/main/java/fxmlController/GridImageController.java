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
            boolean alreadyExecuted = false;
            Frame frame;
            while (!this.owner.getDisabledWebCam()) {
                try {
                    frame = grabber.grabFrame();
                    setFrameToImageView(frame);
                    if(!alreadyExecuted && frame.image.length > 5) {
                        setDescriptionCam();
                        alreadyExecuted = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                grabber.stop();

            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            this.owner.resetImageDescription();
        });

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
            while (!this.owner.getDisabledWebCam()) {
                try {
                    Thread.sleep(this.owner.getSpinnerTime());
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(this.getImageView().getImage(), null);

                    byte[] bytes = toByteArray(bufferedImage, "jpg");


                    TensorFlowUtils tensorFlowUtils = new TensorFlowUtils();
                    Tensor<Float> tensor = tensorFlowUtils.executeModelFromByteArray(
                            this.owner.graphDef,
                            tensorFlowUtils.byteBufferToTensor(bytes)
                    );
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
