package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProcessController {

    private final AnalyzeService analyzeService;

    @GetMapping("/process")
    public ResponseEntity<?> process() {
        analyzeService.captureFromVideo();
        return ResponseEntity.ok("In progress...");
    }

}
