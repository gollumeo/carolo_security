package com.example.demo.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JsonResponse {
    private String url;
    private String type;
    private String description;
    private String status;
    private double confidence;

}