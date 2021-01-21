package fxmlController;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.text.Text;
import javax.imageio.ImageIO;
import javax.swing.Timer;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;


import org.tensorflow.Tensor;

import utils.ImageDescription;
import utils.TensorFlowUtils;
import utils.Utils;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;


public class GridImageController implements Initializable {
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

    private final TensorFlowUtils tensorFlowUtils;
    private Timer timerCamDescription;
    private int timerDelay;

    public void setOwner(ApplicationController owner) {
        this.owner = owner;
    }

    public Frame getFrame() {
        return this.frame;
    }

    public ImageView getImageView() {
        return this.imageView;
    }

    /**
     * GridImageController constructor
     */
    public GridImageController() {
        tensorFlowUtils = new TensorFlowUtils();
    }

    /**
     * Call after the controller was created.
     * @param url url
     * @param resources resources
     */
    @Override
    public void initialize(URL url, ResourceBundle resources) { }

    /**
     * Set the description from an image
     * @param imageDescription The data if the picture
     */
    public void setDescription(ImageDescription imageDescription) {
        this.textObject.setText(imageDescription != null ? "Object: " + imageDescription.getLabel() : "Object: ");
        this.textIndex.setText(imageDescription != null ? "Index: " + imageDescription.getIndex() : "Index: ");
        this.textProbability.setText(imageDescription != null ? "Probability: " + Utils.round(imageDescription.getProbability() * 100, 2) + "%" : "Probability: ");
        String separator = this.owner.getOS().contains("win") ? "/" : "//";
        this.imageView.setImage(imageDescription != null ? new Image("file:" + separator + imageDescription.getPath()) : null);
    }

    /**
     * Set the WebCam
     * @throws FrameGrabber.Exception if the device was not found
     */
    public void setCam() throws FrameGrabber.Exception {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(2);
        grabber.start();

        timerDelay = this.owner.getSpinnerTime();
        timerCamDescription = new Timer(timerDelay, e -> timerCamDescriptionTick());
        timerCamDescription.start();

        Executors.newSingleThreadExecutor().execute(() -> {

            while (!this.owner.getDisabledWebCam()) {
                if (timerDelay != this.owner.getSpinnerTime()) {
                    timerDelay = this.owner.getSpinnerTime();
                    timerCamDescription.setDelay(timerDelay);
                }
                try {
                    this.frame = grabber.grabFrame();
                    setFrameToImageView(this.frame);
                } catch (IOException e) {
                    System.out.println("Error during WebCam thread.");
                }
            }
            try {
                if (timerCamDescription != null) {
                    timerCamDescription.stop();
                    timerCamDescription = null;
                }
                grabber.stop();
                grabber.close();

            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            this.owner.resetImageDescription();
        });

    }

    /**
     * Set the picture visible
     * @param frame The frame
     */
    private void setFrameToImageView(Frame frame) {
        this.getImageView().setImage(frameToImage(frame));
    }

    /**
     * Get the writable image from a frame
     * @param frame The frame
     * @return The writable image
     */
    private WritableImage frameToImage(Frame frame) {
        BufferedImage bufferedImage = java2DFrameConverter.getBufferedImage(frame);
        try {
            return SwingFXUtils.toFXImage(applyCadreFilter(bufferedImage), null);
        } catch (IOException e) {
            e.printStackTrace();
            return  SwingFXUtils.toFXImage(bufferedImage, null);
        }
    }

    /**
     * The event tick raised by the timer of the WebCam
     */
    private void timerCamDescriptionTick() {
        BufferedImage bufferedImage = java2DFrameConverter.getBufferedImage(frame);
        byte[] bytes = toByteArray(bufferedImage, "jpg");
        if (bytes == null) {
            return;
        }
        Tensor<Float> tensor1 = tensorFlowUtils.byteBufferToTensor(bytes);
        if (tensor1 == null) {
            return;
        }
        Tensor<Float> tensor2 = tensorFlowUtils.executeModelFromByteArray(this.owner.graphDef, tensor1);
        this.owner.setImageDescription(tensorFlowUtils.getDescription(null, tensor2, this.owner.allLabels));
    }

    /**
     * Convert a bufferedImage to a bytes buffer
     * @param bufferedImage The source
     * @param format The format
     * @return The bytes buffer
     */
    public static byte[] toByteArray(BufferedImage bufferedImage, String format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { ImageIO.write(bufferedImage, format, baos); }
        catch (IOException ignored) { return null; }
        return baos.toByteArray();
    }

    private BufferedImage applyCadreFilter(BufferedImage bufferedImage) throws IOException {
        BufferedImage cadreBuffered = ImageIO.read(new File("src/main/resources/images/cadre.png"));
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
        int x = (bufferedImage.getWidth() - cadreBuffered.getWidth()) / 2;
        int y = (bufferedImage.getHeight() - cadreBuffered.getHeight()) / 2;
        g2d.drawImage(cadreBuffered, x, y, null);
        g2d.dispose();
        return bufferedImage;
    }
}
