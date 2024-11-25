package com.happy.observator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.happy.observator.model.TradingOrderProcessor;
import com.happy.observator.service.UserService;

@SpringBootApplication
public class ObservatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObservatorApplication.class, args);
	}

    // 서버-Mysql 연결시 이미 등록된 계정이므로 주석 처리
    // 사용 저장소에 따라 application.properties의 저장소 주석 변경
	@Bean
    CommandLineRunner run(UserService userService, TradingOrderProcessor tradingOrderProcessor) {
        return args -> {
            userService.saveUser("user", "password");
            String jsonOrder = "{\"userId\":1,\"action\":\"buy\",\"amount\":\"5000\",\"targetTime\":\"15:30:00.500\"}";
            tradingOrderProcessor.processReceivedOrder(jsonOrder);
        };
    }
}
