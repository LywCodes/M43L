package ita.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Value("${eai.gateway.auth}")
    private String ssoAuth;

    @Value("${eai.gateway.url}")
    private String ssoUrl;

    private String ssoToken;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedRate = 3300, timeUnit = TimeUnit.SECONDS)
    public void getAccessToken() throws Exception {
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
