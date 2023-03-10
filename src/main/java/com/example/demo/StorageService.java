package com.example.demo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    @Resource(name = "amazonS3")
    private final AmazonS3 amazonS3Client;
    private final String bucketName = "screenshot-ai-citizens";

    public String upload(IplImage receivedUrl) {
        try {

            byte[] b = receivedUrl.imageData().getStringBytes();
            BufferedImage im = new Java2DFrameConverter().convert(new OpenCVFrameConverter.ToIplImage().convert(receivedUrl));


            InputStream inputStream = new ByteArrayInputStream(im.toString().getBytes());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/jpg");
            metadata.setContentLength(b.length);

            String key = bucketName + UUID.randomUUID() + ".jpg";
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, metadata).withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3Client.putObject(putObjectRequest);
            URL s3Url = amazonS3Client.getUrl(bucketName, key);

            inputStream.close();
            System.out.println(s3Url);
            return s3Url.toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public InputStream download(String fileName) {
        try {
            S3Object object = amazonS3Client.getObject(bucketName, fileName);
            return object.getObjectContent();
        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to download the file", e);
        }
    }

    public List<S3ObjectSummary> listObjects() {
        ObjectListing objectListing = amazonS3Client.listObjects(bucketName);
        return objectListing.getObjectSummaries();
    }

    public void deleteObject(String name) {
        amazonS3Client.deleteObject(bucketName, name);
    }
}
