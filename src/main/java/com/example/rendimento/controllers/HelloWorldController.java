package com.example.rendimento.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(HelloWorldController.class);

    @Autowired
    private AppMetadataService appMetadataService;

    @GetMapping("/hello")
    public Map<String, String> hello() {
        log.info("Ricevuta richiesta GET /api/hello");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello World!");
        response.put("status", "success");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("application", "Rendimento Spring Boot Application");
        log.info("Risposta per GET /api/hello: {}", response);
        return response;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        log.info("Ricevuta richiesta GET /api/health");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Recupera i dati di tutte le applicazioni
        List<AppMetadataDTO> applications = appMetadataService.getAllAppMetadata();
        response.put("applications", applications);
        response.put("applicationsCount", applications.size());
        
        log.info("Risposta per GET /api/health: status={}, applicationsCount={}", response.get("status"), applications.size());
        return response;
    }
}
