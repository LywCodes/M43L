package ita.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@EnableScheduling
@Data
public class SsoService {

    private final String ssoAuth;
    private final String ssoUrl;
    private final RestTemplate restTemplate;
    private String ssoToken;

    public SsoService(RestTemplate restTemplate,
                      @Value("${eai.gateway.auth}") String ssoAuth,
                      @Value("${eai.gateway.url}") String ssoUrl
                      ){
        this.restTemplate = restTemplate;
        this.ssoAuth = ssoAuth;
        this.ssoUrl = ssoUrl;
    }

    @Scheduled(fixedRate = 3300, timeUnit = TimeUnit.SECONDS)
    public void getAccessToken() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Basic " + this.ssoAuth);

        MultiValueMap<String, String> ssoBody = new LinkedMultiValueMap<>();

        ssoBody.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(ssoBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(this.ssoUrl, request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        this.ssoToken = objectMapper.readValue(response.getBody(), Map.class).get("access_token").toString();
    }
}
