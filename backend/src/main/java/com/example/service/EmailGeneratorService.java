package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

// import org.springframework.beans.factory.annotation.Value;
import com.example.dto.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.config.GeminiConfig;

import java.util.Map;


// https://email-reply-generator-three.vercel.app/

@Service
public class EmailGeneratorService {

    @Autowired
    public GeminiConfig geminiConfig;

    private final WebClient webClient;   // Its alternative of RestTemplate
    /*
     Use WebClient over RestTemplate because:

        ✅ Non-blocking → handles more requests with less resources (better for high concurrency).

        ✅ Future-proof → RestTemplate is outdated; WebClient is the modern alternative.

        ✅ Reactive support → works with reactive streams (Mono, Flux).

        ✅ Flexible → better error handling, streaming, and customization.
     */

    // @Value("${gemini.api.url}")
    // private String geminiApiUrl;

    // @Value("${gemini.api.key}")
    // private String geminiApiKey;

    // constructor 
    public EmailGeneratorService(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.build();
    }

    public String generateMailReply(EmailRequest emailRequest) {

        // System.out.println("emailRequest::::::"+ emailRequest);
        // Build the prompt
        String prompt = buildPrompt(emailRequest);

        // Craft a request {bcz we need to follow a perfect format to send the api request}
        /*
        REQUIRED FORMAT

        {
            "contents": [
            {
                "parts": [
                {
                    "text": "Explain how AI works in a few words"
                }
                ]
            }
            ]
        }
        */

        // Request body for the API which we are calling internally (AI)
        Map<String, Object> requestBody = 
        Map.of(
            "contents", new Object[] {
                Map.of(
                    "parts", new Object[] {
                        Map.of(
                            "text", prompt
                        )
                    }
                )
            }
        );

        // Do request and get response from google API
        
        String response = webClient.post()
        .uri(geminiConfig.geminiApiUrl + geminiConfig.geminiApiKey)
        .header("Content-Type", "application/json")
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class)
        .block();

    
        // extract & return response
        // response is in json format so we need to extract the exact response

       
        String reply = extractResponseContent(response);

        // System.out.println("Email reply:::::" + reply);
        return reply;
    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            return "Error in processing :" + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder(); //  Why Use StringBuilder Over String? String is immutable and StringBuilder is mutable 
        prompt.append("Generate the email reply for the following email content. Please don't generate the subject");
        if(emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append("Use a ").append(emailRequest.getTone()).append("tone. ");
        }

        prompt.append("\n Original email: \n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }
    
}
