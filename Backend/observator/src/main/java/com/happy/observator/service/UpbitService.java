package com.happy.observator.service;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    private static final String UPBIT_ORDER_URL = "https://api.upbit.com/v1/orders";
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

    public String placeBuyOrder(String accessKey, String secretKey, String market, String price) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("market", market);
            params.put("side", "bid");
            params.put("price", price);
            params.put("ord_type", "price");

            ArrayList<String> queryElements = new ArrayList<>();
            for(Map.Entry<String, String> entity : params.entrySet()) {
                queryElements.add(entity.getKey() + "=" + entity.getValue());
            }

            String queryString = String.join("&", queryElements.toArray(new String[0]));

            String jwtToken = UpbitApiUtil.generateJwtToken(accessKey, secretKey, queryString);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(params), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    UPBIT_ORDER_URL, HttpMethod.POST, entity, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();  // Return the response body on success
            } else {
                throw new RuntimeException("Order failed: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error placing buy order: " + e.getMessage(), e);
        }
    }
}
