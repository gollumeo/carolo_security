package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageAnalysis;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageCaption;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.VisualFeatureTypes;
import lombok.RequiredArgsConstructor;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class Service {


    private final StorageService storageService;

    static String subscriptionKey = "cbc9d195544f46268ad3fcb4b02720f5";
    static String endpoint = "https://test-major-us.cognitiveservices.azure.com/";

    public void captureFromVideo() {
        new Thread(() -> {
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("video.mp4")) {
                grabber.start();

                // Capture frame every 3 seconds
                int frameInterval = (int) Math.round(grabber.getFrameRate() * 3.0);
                int frameNumber = 0;
                while (true) {
                    grabber.setFrameNumber(frameNumber);

                    try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                        Frame frame = grabber.grabImage();
                        if (frame == null) {
                            break;
                        }
                        BufferedImage image = converter.convert(frame);

                        // Save the image as a JPEG file
                        File output = new File("frame-" + frameNumber + ".jpg");
                        ImageIO.write(image, "jpg", output);
                        System.out.println("Saved " + output);
                        processResponse(image);

                    }

                    frameNumber += frameInterval;
                }

                grabber.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    //schedule every 5 seconds
    //@Scheduled(cron = "*/5 * * * * *")
/*    private void captureFromWebcam() {
        new Thread(() -> {
            try {
                CanvasFrame canvas = new CanvasFrame("Web Cam");
                canvas.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                FrameGrabber grabber = new OpenCVFrameGrabber(0);
                OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

                grabber.start();
                Frame frame = grabber.grab();

                IplImage img = converter.convert(frame);
                String filename = "screenshot_" + UUID.randomUUID() + ".jpg";
                cvSaveImage(filename, img);

                System.out.println("Image " + filename + " saved");

                Thread.sleep(2000);
                canvas.dispatchEvent(new WindowEvent(canvas, WindowEvent.WINDOW_CLOSING));
                grabber.stop();


                processResponse(img);
            } catch (JsonProcessingException | FrameGrabber.Exception | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }*/

    private void processResponse(BufferedImage filename) throws JsonProcessingException {
        String url = savePictureToAmazonS3Bucket(filename);
        sendLinkOfAmazonS3PictureToMicrosoftAzureVisionAPI(url);
    }


    // save the picture to amazon s3 bucket
    private String savePictureToAmazonS3Bucket(BufferedImage filename) {
        return storageService.upload(filename);
    }

    // send the link of the amazon s3 picture to Microsoft Azure Vision API
    private void sendLinkOfAmazonS3PictureToMicrosoftAzureVisionAPI(String body) {
        // TODO

        ComputerVisionClient compVisClient = Authenticate(subscriptionKey, endpoint);
        AnalyzeRemoteImage(compVisClient, body);
    }

    public static ComputerVisionClient Authenticate(String subscriptionKey, String endpoint) {
        return ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
    }


    public static void AnalyzeRemoteImage(ComputerVisionClient compVisClient, String url) {
        /*
         * Analyze an image from a URL:
         *
         * Set a string variable equal to the path of a remote image.
         */
        String pathToRemoteImage = "https://github.com/Azure-Samples/cognitive-services-sample-data-files/raw/master/ComputerVision/Images/faces.jpg";

        // This list defines the features to be extracted from the image.
        List<VisualFeatureTypes> featuresToExtractFromRemoteImage = new ArrayList<>();
        featuresToExtractFromRemoteImage.add(VisualFeatureTypes.DESCRIPTION);

        System.out.println("\n\nAnalyzing an image from a URL ..." + url);

        try {
            // Call the Computer Vision service and tell it to analyze the loaded image.
            ImageAnalysis analysis = compVisClient.computerVision().analyzeImage().withUrl(url)
                    .withVisualFeatures(featuresToExtractFromRemoteImage).execute();

            // Display image tags and confidence values.
            for (ImageCaption caption : analysis.description().captions()) {
                System.out.printf("'%s' with confidence %f\n", caption.text(), caption.confidence());

                if (caption.text().contains("gun") || caption.text().contains("weapon") || caption.text().contains("knife")) {
                    System.out.println("ALERT");
                    //sendAlertToAdminPanel(jsonObject);
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
    // END - Analyze an image from a URL.

    // process the response from Microsoft Azure Vision API
    /*private void processResponseFromMicrosoftAzureVisionAPI(String jsonResponse, String fileName) throws JsonProcessingException {
        // TODO
        // if jsonResponse contains list of defined object like gun, weapon, knife, etc then send alert to the admin panel
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(jsonResponse);
        System.out.println(node);
        JsonNode objects = node.get("objects");
        for (JsonNode object : objects) {
            String objectName = object.get("object").asText();
            if (objectName.equals("gun") || objectName.equals("weapon") || objectName.equals("knife")) {
                sendAlertToAdminPanel(jsonResponse);
                return;
            } else {
                deletePictureFromAmazonS3Bucket(fileName);
            }
        }
    }
*/

    // if response contains list of defined object like gun, weapon, knife, etc then send alert to the admin panel
    // alert should contain the picture link, the time, the location, the object detected
    // alert should be sent to the admin panel using web socket
    private void sendAlertToAdminPanel(String jsonResponse) {
        // TODO
        // admin panel should be able to see the alert in real time
        // admin panel should be able to see the alert history
        // admin panel should be able to see the alert history in real time (if new alert comes in, it should be shown in real time)
    }

    // if response does not contain list of defined object like gun, weapon, knife, etc then delete the picture from amazon s3 bucket
    private void deletePictureFromAmazonS3Bucket(String imageName) {
        // TODO
    }

}
