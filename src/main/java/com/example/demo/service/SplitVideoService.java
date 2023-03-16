package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SplitVideoService {

    private final AnalyzeService analyzeService;

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
                        analyzeService.processResponse(image);

                    }

                    frameNumber += frameInterval;
                }

                grabber.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
