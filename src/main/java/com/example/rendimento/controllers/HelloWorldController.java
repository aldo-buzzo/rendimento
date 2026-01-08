package com.example.rendimento.controllers;

import com.example.rendimento.dto.AppMetadataDTO;
import com.example.rendimento.service.AppMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloWorldController {

    @Autowired
    private AppMetadataService appMetadataService;

    @GetMapping("/hello")
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello World!");
        response.put("status", "success");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("application", "Rendimento Spring Boot Application");
        return response;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Recupera i dati di tutte le applicazioni
        List<AppMetadataDTO> applications = appMetadataService.getAllAppMetadata();
        response.put("applications", applications);
        response.put("applicationsCount", applications.size());
        
        return response;
    }
}
