package com.example.demo.service;

import com.example.demo.config.SensitiveWords;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AnalyzeService {
    private final SimpMessageSendingOperations messagingTemplate;

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

    private void processResponse(BufferedImage filename) throws JsonProcessingException {
        String url = savePictureToAmazonS3Bucket(filename);
        AnalyzeRemoteImage(url);
    }


    // save the picture to amazon s3 bucket
    private String savePictureToAmazonS3Bucket(BufferedImage filename) {
        return storageService.upload(filename);
    }


    public static ComputerVisionClient Authenticate(String subscriptionKey, String endpoint) {
        return ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
    }


    public void AnalyzeRemoteImage(String url) {
        HttpClient httpclient = HttpClients.createDefault();

        try {
            URIBuilder builder = new URIBuilder("https://test-major-us.cognitiveservices.azure.com/computervision/imageanalysis:analyze?api-version=2023-02-01-preview");

            builder.setParameter("features", "denseCaptions");
            builder.setParameter("language", "en");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);
            String json = "{ \"url\": \"" + url + "\" }";
            // Request body
            StringEntity reqEntity = new StringEntity(json);
            request.setEntity(reqEntity);
            System.out.println("\n\nAnalyzing an image from a URL ..." + url);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.readTree(EntityUtils.toString(entity));
                JsonNode denseCaptionResults = node.get("denseCaptionsResult").get("values");

                for (JsonNode denseCaptionResult : denseCaptionResults) {
                    String text = denseCaptionResult.get("text").asText();
                    System.out.println(text);
                    //create a list of words to check for
                    List<String> words = SensitiveWords.sensitivesWords();

                    for (String word : words) {
                        if (text.toLowerCase().contains(word.toLowerCase())) {
                            String jsonResponse = "{\"url\": \"" + url + "\", \"text\": \"" + text + "\"}"; //todo use data model
                            sendAlertToAdminPanel(jsonResponse);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private void sendAlertToAdminPanel(String jsonResponse) {
        messagingTemplate.convertAndSend("/topic", jsonResponse);
        System.out.println("alert sent to admin panel");


    }
}
