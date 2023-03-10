package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageAnalysis;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageTag;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.VisualFeatureTypes;
import lombok.RequiredArgsConstructor;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.springframework.scheduling.annotation.Scheduled;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bytedeco.opencv.helper.opencv_imgcodecs.cvSaveImage;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class Service {


    private final StorageService storageService;

    static String subscriptionKey = "cbc9d195544f46268ad3fcb4b02720f5";
    static String endpoint = "https://test-major-us.cognitiveservices.azure.com/";

    //schedule every 5 seconds
    @Scheduled(cron = "*/10 * * * * *")
    private void extracted() {
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
    }

    private void processResponse(IplImage filename) throws JsonProcessingException {
        String url = savePictureToAmazonS3Bucket(filename);
        String jsonResponse = sendLinkOfAmazonS3PictureToMicrosoftAzureVisionAPI(url);
        processResponseFromMicrosoftAzureVisionAPI(jsonResponse, url);
    }


    // save the picture to amazon s3 bucket
    private String savePictureToAmazonS3Bucket(IplImage filename) {
        // TODO
        return storageService.upload(filename);
    }

    // send the link of the amazon s3 picture to Microsoft Azure Vision API
    private String sendLinkOfAmazonS3PictureToMicrosoftAzureVisionAPI(String body) {
        // TODO

        ComputerVisionClient compVisClient = Authenticate(subscriptionKey, endpoint);
        ImageAnalysis imageAnalysis = AnalyzeRemoteImage(compVisClient, body);

        assert imageAnalysis != null;
        return imageAnalysis.toString();
    }

    public static ComputerVisionClient Authenticate(String subscriptionKey, String endpoint) {
        return ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
    }


    public static ImageAnalysis AnalyzeRemoteImage(ComputerVisionClient compVisClient, String url) {
        /*
         * Analyze an image from a URL:
         *
         * Set a string variable equal to the path of a remote image.
         */
        String pathToRemoteImage = "https://github.com/Azure-Samples/cognitive-services-sample-data-files/raw/master/ComputerVision/Images/faces.jpg";

        // This list defines the features to be extracted from the image.
        List<VisualFeatureTypes> featuresToExtractFromRemoteImage = new ArrayList<>();
        featuresToExtractFromRemoteImage.add(VisualFeatureTypes.TAGS);

        System.out.println("\n\nAnalyzing an image from a URL ..." + url);

        try {
            // Call the Computer Vision service and tell it to analyze the loaded image.
            ImageAnalysis analysis = compVisClient.computerVision().analyzeImage().withUrl(url)
                    .withVisualFeatures(featuresToExtractFromRemoteImage).execute();


            // Display image tags and confidence values.
            System.out.println("\nTags: ");
            for (ImageTag tag : analysis.tags()) {
                System.out.printf("'%s' with confidence %f\n", tag.name(), tag.confidence());
            }

            return analysis;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
    // END - Analyze an image from a URL.

    // process the response from Microsoft Azure Vision API
    private void processResponseFromMicrosoftAzureVisionAPI(String jsonResponse, String fileName) throws JsonProcessingException {
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
