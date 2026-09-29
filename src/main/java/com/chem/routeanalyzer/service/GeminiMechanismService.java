package com.chem.routeanalyzer.service;

import com.chem.routeanalyzer.model.DetailedMechanism;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
public class GeminiMechanismService {
    private static final Logger log = LoggerFactory.getLogger(GeminiMechanismService.class);
    
    @Value("${gemini.api.key:}")
    private String defaultApiKey;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiMechanismService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com/v1beta").build();
        this.objectMapper = new ObjectMapper();
    }

    private Retry createExponentialBackoffSpec() {
        return Retry.backoff(4, Duration.ofMillis(1500))
                .maxBackoff(Duration.ofSeconds(15))
                .jitter(0.5)
                .filter(this::isTransientException)
                .doBeforeRetry(retrySignal -> {
                    Throwable failure = retrySignal.failure();
                    int statusCode = (failure instanceof WebClientResponseException wce) ? wce.getStatusCode().value() : -1;
                    log.warn("Gemini API transient failure (Status: {}). Retrying attempt {}/4", statusCode, retrySignal.totalRetries() + 1);
                });
    }

    private boolean isTransientException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            int code = ex.getStatusCode().value();
            return code == 429 || code == 503 || code == 502 || code == 504;
        }
        return throwable instanceof IOException || throwable instanceof TimeoutException;
    }

    public boolean validateApiKey(String apiKey) {
        String keyToTest = (apiKey != null && !apiKey.isBlank()) ? apiKey.trim() : defaultApiKey;
        if (keyToTest == null || keyToTest.isBlank()) throw new IllegalArgumentException("API key cannot be blank.");

        try {
            webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/models/gemini-2.5-flash").queryParam("key", keyToTest).build())
                    .retrieve().toBodilessEntity()
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(800)).filter(this::isTransientException))
                    .block();
            return true;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 400 || e.getStatusCode().value() == 403) {
                throw new IllegalArgumentException("Invalid API key or unauthorized access.");
            }
            throw new RuntimeException("Google API error: " + e.getStatusCode().value());
        }
    }

    public DetailedMechanism explainMechanism(String reactionSmiles, String clientApiKey) {
        String activeKey = (clientApiKey != null && !clientApiKey.isBlank()) ? clientApiKey.trim() : defaultApiKey;
        if (activeKey == null || activeKey.isBlank()) throw new IllegalArgumentException("No Gemini API Key provided.");

        String prompt = """
            Act as an organic chemistry reaction mechanism specialist. Analyze this transformation: %s
            Break it down into an elementary step-by-step mechanism. 
            For every step provide:
            1. Step number and descriptive title
            2. Intermediate SMILES
            3. Frontier Molecular Orbital (FMO) interaction (e.g. HOMO(lone pair) -> LUMO(pi*))
            4. Thermodynamic & kinetic driving force
            5. Detailed electron movement explanation
            6. Lists of bonds formed and bonds broken
            Return strictly valid JSON matching the schema.
            """.formatted(reactionSmiles);

        Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("response_mime_type", "application/json"));

        try {
            Map<?, ?> response = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/models/gemini-2.5-flash:generateContent").queryParam("key", activeKey).build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody).retrieve().bodyToMono(Map.class)
                    .retryWhen(createExponentialBackoffSpec())
                    .block();

            List<?> candidates = (List<?>) response.get("candidates");
            String jsonText = (String) ((Map<?, ?>) ((List<?>) ((Map<?, ?>) candidates.get(0)).get("content")).get("parts")).get(0).get("text");
            return objectMapper.readValue(jsonText, DetailedMechanism.class);
        } catch (Exception e) {
            throw new RuntimeException("Mechanism Elucidation Error: " + e.getMessage(), e);
        }
    }
}
