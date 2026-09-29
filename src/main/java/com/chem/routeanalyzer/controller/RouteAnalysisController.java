package com.chem.routeanalyzer.controller;

import com.chem.routeanalyzer.model.*;
import com.chem.routeanalyzer.service.GeminiMechanismService;
import com.chem.routeanalyzer.service.RouteAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteAnalysisController {
    private final RouteAnalysisService routeAnalysisService;
    private final GeminiMechanismService geminiMechanismService;

    public RouteAnalysisController(RouteAnalysisService routeAnalysisService, GeminiMechanismService geminiMechanismService) {
        this.routeAnalysisService = routeAnalysisService;
        this.geminiMechanismService = geminiMechanismService;
    }

    @PostMapping("/verify-key")
    public ResponseEntity<?> verifyApiKey(@RequestHeader(value = "X-Gemini-Api-Key", required = false) String customApiKey) {
        try {
            boolean valid = geminiMechanismService.validateApiKey(customApiKey);
            return ResponseEntity.ok(Map.of("valid", valid, "message", "API key is valid."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("valid", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeRoute(@RequestBody AnalysisRequest request, @RequestHeader(value = "X-Gemini-Api-Key", required = false) String customApiKey) {
        if (request.getReactionSmilesSteps() == null || request.getReactionSmilesSteps().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reaction steps cannot be empty."));
        }
        try {
            AnalysisResponse metrics = routeAnalysisService.analyzeRoute(request);
            DetailedMechanism mechanism = geminiMechanismService.explainMechanism(request.getReactionSmilesSteps().get(0), customApiKey);

            Map<String, Object> combinedResult = new HashMap<>();
            combinedResult.put("metrics", metrics);
            combinedResult.put("mechanism", mechanism);
            return ResponseEntity.ok(combinedResult);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
