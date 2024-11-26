package com.happy.observator.model;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ProxyController {

    @GetMapping("/proxy/upbit/candles")
    public Object getCandles(@RequestParam String market, @RequestParam int count) {
        String url = "https://api.upbit.com/v1/candles/minutes/1?market=" + market + "&count=" + count;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, Object.class);
    }

    @GetMapping("/proxy/upbit/orderbook")
    public Object getOrderbook(@RequestParam String markets) {
        String url = "https://api.upbit.com/v1/orderbook?markets=" + markets;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, Object.class);
    }

    @GetMapping("/proxy/upbit/ticker")
    public Object getTicker(@RequestParam String markets) {
        String url = "https://api.upbit.com/v1/ticker?markets=" + markets;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, Object.class);
    }

    @GetMapping("/account")
    public Object getAccountInfo() {
        String url = "https://api.upbit.com/v1/accounts";
        RestTemplate restTemplate = new RestTemplate();

        // 인증 헤더 추가 (필수)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer YOUR_ACCESS_TOKEN"); // 개인 API 키 추가

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, Object.class).getBody();
    }

}
