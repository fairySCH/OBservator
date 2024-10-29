package com.happy.observator.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.happy.observator.Upbit.UpbitApiUtil;
import com.happy.observator.Upbit.UpbitBalance;

@Service
public class UpbitService {
    private static final String UPBIT_BALANCE_URL = "https://api.upbit.com/v1/accounts";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<UpbitBalance> getBalances(String accessKey, String secretKey) {
        try {
            // Create a unique nonce value
            String jwtToken = UpbitApiUtil.generateJwtToken(accessKey, secretKey, "");

            // Set up headers with authorization info
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            headers.set("Content-Type", "application/json");

            // Create the request entity with headers
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Send the GET request to Upbit
            ResponseEntity<UpbitBalance[]> response = restTemplate.exchange(UPBIT_BALANCE_URL, HttpMethod.GET, entity, UpbitBalance[].class);

            // Parse the response body into a list of balances
            return Arrays.asList(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch balance info: " + e.getMessage());
        }
    }
}
