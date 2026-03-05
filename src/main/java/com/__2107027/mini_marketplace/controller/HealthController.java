package com.__2107027.mini_marketplace.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides application health status without requiring authentication
 * Used by monitoring systems, load balancers, and DevOps tools
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Value("${spring.application.name:mini-marketplace}")
    private String applicationName;

    @Value("${server.port:8082}")
    private String serverPort;

    /**
     * Basic health check endpoint
     * Returns simple status for quick health verification
     * 
     * @return ResponseEntity with basic health status
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health check endpoint
     * Returns comprehensive application information
     * 
     * @return ResponseEntity with detailed health information
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> healthDetailed() {
        Map<String, Object> response = new HashMap<>();
        
        // Basic status
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Application info
        Map<String, Object> application = new HashMap<>();
        application.put("name", applicationName);
        application.put("port", serverPort);
        application.put("profile", System.getProperty("spring.profiles.active", "default"));
        response.put("application", application);
        
        // System info
        Map<String, Object> system = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("totalMemory", runtime.totalMemory() / (1024 * 1024) + " MB");
        system.put("freeMemory", runtime.freeMemory() / (1024 * 1024) + " MB");
        system.put("maxMemory", runtime.maxMemory() / (1024 * 1024) + " MB");
        system.put("availableProcessors", runtime.availableProcessors());
        response.put("system", system);
        
        // Build info (if available)
        Map<String, Object> build = new HashMap<>();
        build.put("version", "0.0.1-SNAPSHOT");
        build.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("build", build);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Liveness probe endpoint
     * Indicates if the application is running and alive
     * Used by Kubernetes liveness probes
     * 
     * @return ResponseEntity with liveness status
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> liveness() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Application is alive");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Readiness probe endpoint
     * Indicates if the application is ready to serve traffic
     * Used by Kubernetes readiness probes and load balancers
     * 
     * @return ResponseEntity with readiness status
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> readiness() {
        Map<String, String> response = new HashMap<>();
        
        try {
            // You can add specific readiness checks here:
            // - Database connectivity
            // - External service availability
            // - Cache warmup status
            // For now, we'll assume the app is ready if it can respond
            
            response.put("status", "UP");
            response.put("message", "Application is ready to serve traffic");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("message", "Application is not ready: " + e.getMessage());
            
            return ResponseEntity.status(503).body(response);
        }
    }
}