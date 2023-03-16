package com.example.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.bytedeco.opencv.helper.opencv_imgcodecs.cvSaveImage;

@Service
@RequiredArgsConstructor
public class SplitCameraService {

    private final AnalyzeService analyzeService;

    @Resource(name = "canvas")
    private final CanvasFrame canvas;

    @Resource(name = "grabber")
    private final FrameGrabber grabber;


    @PostConstruct
    private void displayWebCam() {
        new Thread(() -> {
            try {
                while (canvas.isVisible()) {
                    Frame frame = grabber.grab();
                    canvas.showImage(frame);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }


    @Scheduled(cron = "*/30 * * * * *")
    private void captureFromWebcam() throws InterruptedException {
        System.out.println("Start capture");
        Thread.sleep(1000);
        System.out.println("3");
        Thread.sleep(1000);
        System.out.println("2");
        Thread.sleep(1000);
        System.out.println("1");
        Thread.sleep(1000);

        new Thread(() -> {
            try (OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
                 Java2DFrameConverter imageConverter = new Java2DFrameConverter()) {

                Frame frame = grabber.grab();

                IplImage image = converter.convert(frame);
                String filename = "screenshot_" + UUID.randomUUID() + ".jpg";
                cvSaveImage(filename, image);

                System.out.println("Image " + filename + " saved");

                analyzeService.processResponse(imageConverter.getBufferedImage(converter.convert(image)));

            } catch (JsonProcessingException | FrameGrabber.Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
