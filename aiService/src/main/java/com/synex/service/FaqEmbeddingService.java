package com.synex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class FaqEmbeddingService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final JdbcTemplate jdbcTemplate;

    public FaqEmbeddingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    private final Map<String, String> faqMap = Map.of(
            "do you allow pets", "Yes, we allow pets with a small fee.",
            "what is the check-in time", "Check-in time is 2 PM.",
            "do you have wifi", "Yes, we offer free Wi-Fi in all rooms."
            // Add more FAQs
        );

    public String getMatchingFaqAnswer(String userInput) {
        LevenshteinDistance distance = new LevenshteinDistance();
        String cleanedInput = userInput.trim().toLowerCase();

        int minDistance = Integer.MAX_VALUE;
        String bestAnswer = null;

        for (Map.Entry<String, String> entry : faqMap.entrySet()) {
            String question = entry.getKey().toLowerCase();
            int dist = distance.apply(cleanedInput, question);

            // You can tune the threshold below (e.g., 5 means very close match)
            if (dist < 5 && dist < minDistance) {
                minDistance = dist;
                bestAnswer = entry.getValue();
            }
        }

        return bestAnswer; // May return null if nothing matched closely
    }

    private List<Double> getEmbedding(String text) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> body = new HashMap<>();
            body.put("model", "text-embedding-ada-002");
            body.put("input", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/embeddings",
                request,
                String.class
            );

            JsonNode node = mapper.readTree(response.getBody());
            JsonNode embeddingNode = node.path("data").get(0).path("embedding");

            List<Double> embedding = new ArrayList<>();
            for (JsonNode val : embeddingNode) {
                embedding.add(val.asDouble());
            }
            return embedding;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public void embedAndSaveFaq(String question, String answer) {
        List<Double> embedding = getEmbedding(question);
        if (embedding.isEmpty()) return;

        // Convert List<Double> to array (depending on your JDBC & DB driver, you might need to convert this differently)
        Double[] vectorArray = embedding.toArray(new Double[0]);

        String sql = "INSERT INTO faq_vector (question, answer, embedding) VALUES (?, ?, ?)";

        jdbcTemplate.update(sql, question, answer, vectorArray);
    }

}

