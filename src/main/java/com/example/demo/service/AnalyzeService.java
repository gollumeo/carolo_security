package com.example.demo.service;

import com.example.demo.config.SensitiveWords;
import com.example.demo.model.JsonResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AnalyzeService {


    @Value("${azure.sub.key}")
    private String subscriptionKey;

    @Value("${azure.endpoint}")
    private String endpoint;

    private final SimpMessageSendingOperations messagingTemplate;
    private final StorageService storageService;


    public void processResponse(BufferedImage filename) throws JsonProcessingException {
        String url = savePictureToAmazonS3Bucket(filename);
        AnalyzeRemoteImage(url);
    }

    private String savePictureToAmazonS3Bucket(BufferedImage filename) {
        return storageService.upload(filename);
    }

    private void AnalyzeRemoteImage(String url) {
        HttpClient httpclient = HttpClients.createDefault();

        try {
            URIBuilder builder = new URIBuilder("https://" + endpoint + "/computervision/imageanalysis:analyze?api-version=2023-02-01-preview");

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
                    double confidence = denseCaptionResult.get("confidence").asDouble();
                    System.out.println(text);
                    List<String> words = SensitiveWords.sensitivesWords();

                    //todo uncomment this to check if the text contains any of the words in the list
                    //for (String word : words) {
                    //if (text.toLowerCase().contains(word.toLowerCase())) {
                    JsonResponse jsonResponse = JsonResponse.builder().url(url).description(text).confidence(confidence).type(null).build();
                    sendAlertToAdminPanel(jsonResponse);
                    //}
                    //}
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void sendAlertToAdminPanel(JsonResponse jsonResponse) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String response = mapper.writeValueAsString(jsonResponse);
        messagingTemplate.convertAndSend("/topic", response);
        System.out.println("alert sent to admin panel");
    }
}
