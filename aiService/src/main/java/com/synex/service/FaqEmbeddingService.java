package com.synex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public String getMatchingFaqAnswer(String userInput) {
        List<Double> vector = getEmbedding(userInput);
        if (vector.isEmpty()) return null;

        String vectorStr = "[" + vector.stream()
            .map(String::valueOf)
            .reduce((a, b) -> a + "," + b).orElse("") + "]";

        String sql = """
            SELECT answer
            FROM faq_vector
            ORDER BY embedding <=> ?::vector ASC
            LIMIT 1
        """;

        List<String> result = jdbcTemplate.queryForList(sql, new Object[]{vectorStr}, String.class);
        return result.isEmpty() ? null : result.get(0);
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

