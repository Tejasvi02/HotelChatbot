package com.synex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HotelEmbeddingService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HotelEmbeddingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    // 1. Get embedding from OpenAI
    private List<Double> getEmbedding(String inputText) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "text-embedding-ada-002");
            requestBody.put("input", inputText);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/embeddings",
                request,
                String.class
            );

            JsonNode json = mapper.readTree(response.getBody());
            JsonNode embeddingNode = json.path("data").get(0).path("embedding");

            List<Double> vector = new ArrayList<>();
            for (JsonNode val : embeddingNode) {
                vector.add(val.asDouble());
            }
            return vector;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // 2. Save embedding to DB
    public void embedAndSaveHotel(int hotelId, String description) {
        List<Double> vector = getEmbedding(description);
        Double[] vectorArray = vector.toArray(new Double[0]);

        String sql = "INSERT INTO hotel_vectors (hotel_id, embedding) VALUES (?, ?)";

        jdbcTemplate.update(sql, hotelId, vectorArray);
    }

    // 3. Query similar hotels
    public List<Map<String, Object>> findSimilarHotels(String query) {
        List<Double> vector = getEmbedding(query);
        String vectorStr = formatVector(vector); // Format as "[0.1,0.2,...]"

        String sql = "SELECT hotel_id, embedding <=> ?::vector AS distance " +
                     "FROM hotel_vectors " +
                     "ORDER BY distance ASC LIMIT 5";

        return jdbcTemplate.queryForList(sql, vectorStr);
    }

    
    private String formatVector(List<Double> vector) {
        return "[" + vector.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }

}
