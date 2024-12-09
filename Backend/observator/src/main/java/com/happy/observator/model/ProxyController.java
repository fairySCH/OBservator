package com.happy.observator.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ProxyController {

    private static final String BASE_URL = "https://api.upbit.com/v1";

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/proxy/upbit/candles")
    public ResponseEntity<Object> getCandles(@RequestParam String market, @RequestParam int count) {
        String url = BASE_URL + "/candles/days?market=" + market + "&count=" + count;
        try {
            Object response = restTemplate.getForObject(url, Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching candles: " + e.getMessage());
        }
    }

    @GetMapping("/proxy/upbit/orderbook")
    public ResponseEntity<Object> getOrderbook(@RequestParam String markets) {
        String url = BASE_URL + "/orderbook?markets=" + markets;
        try {
            Object response = restTemplate.getForObject(url, Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching orderbook: " + e.getMessage());
        }
    }

    @GetMapping("/proxy/upbit/ticker")
    public ResponseEntity<Object> getTicker(@RequestParam String markets) {
        String url = BASE_URL + "/ticker?markets=" + markets;
        try {
            Object response = restTemplate.getForObject(url, Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching ticker: " + e.getMessage());
        }
    }
} 