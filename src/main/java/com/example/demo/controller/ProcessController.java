package com.example.demo.controller;

import com.example.demo.service.SplitVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProcessController {

    private final SplitVideoService splitVideoService;

    @GetMapping("/process")
    public ResponseEntity<?> process() {
        splitVideoService.captureFromVideo();
        return ResponseEntity.ok("In progress...");
    }

}
