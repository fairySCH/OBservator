package com.happy.observator.model;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.happy.observator.repository.OrderRepositary;
import com.happy.observator.repository.UserRepositary;
import com.happy.observator.service.UpbitService;

@Service
public class TradingOrderProcessor {
    private final OrderRepositary orderRepository;
    private final UpbitService upbitService;
    private final UserRepositary userRepositary;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TradingOrderProcessor(OrderRepositary orderRepository, UpbitService upbitService, ObjectMapper objectMapper, UserRepositary userRepositary) {
        this.orderRepository = orderRepository;
        this.upbitService = upbitService;
        this.objectMapper = objectMapper;
        this.userRepositary = userRepositary;
    }

    public void processReceivedOrder(String receivedJson) {
        try {
            // Parse JSON to OrderRequest
            OrderRequest orderRequest = objectMapper.readValue(receivedJson, OrderRequest.class);

            // Validate and process the order
            //scheduleOrder(orderRequest.getUserId(), orderRequest.getAction(), orderRequest.getAmount(), orderRequest.getExecute_time());
            Order(orderRequest.getUserId(), orderRequest.getAction(), orderRequest.getAmount());
        } catch (Exception e) {
            System.err.println("Failed to process received order: " + e.getMessage());
        }
    }

    public String Order(int userId, String action, String amount) {
        User user = userRepositary.findById(userId);
        if (user != null) {
            System.out.println(user.getUsername());
        } else {
            System.out.println("User not found");
        }

        try {
            String response;
            if ("buy".equalsIgnoreCase(action)) {
                response = upbitService.placeBuyOrder(user.getUpbitAccessKey(), user.getUpbitSecretKey(), "KRW-BTC", amount);
                System.out.println("Buy order placed successfully: " + response);
            } else if ("sell".equalsIgnoreCase(action)) {
                response = upbitService.placeSellOrder(user.getUpbitAccessKey(), user.getUpbitSecretKey(), "KRW-BTC", amount);
                System.out.println("Sell order placed successfully: " + response);
            } else {
                System.out.println("Invalid action. Please specify 'buy' or 'sell'.");
                return "redirect:/trade";
        }
        } catch (Exception e) {
            System.err.println("Failed to place sell order: " + e.getMessage());
        }

        return "redirect:/trade";  // Render the same page with success or error message
    }

    public void scheduleOrder(int userId, String action, String amount, String targetTime) {
        User user = userRepositary.findById(userId);
        if (user != null) {
            System.out.println(user.getUsername());
        } else {
            System.out.println("User not found");
        }
        // Create a unique ID for the order
        String orderId = UUID.randomUUID().toString();

        // Check if there's an existing order for the target time and delete it
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        LocalTime target = LocalTime.parse(targetTime, formatter);
        Optional<S_Order> existingOrder = orderRepository.findByUserIdAndTargetTime(userId, target);
        if (existingOrder.isPresent()) {
                orderRepository.delete(existingOrder.get());
                System.out.println("Deleted existing order: " + existingOrder);
        }

        // Save new order
        S_Order order = new S_Order(orderId, userId, action, amount, "KRW-BTC", target);
        orderRepository.save(order);

        LocalTime now = LocalTime.now();
        long delay = Duration.between(now, target).toMillis();
        
        if (delay < 0) {
            System.out.println("Target time has already passed.");
            return;
        }

        scheduler.schedule(() -> executeOrder(order, user), delay, TimeUnit.MILLISECONDS);
        System.out.println("Scheduled " + action + " order for " + targetTime + " for user " + userId);

        return;  // Render the same page with success or error message
    }

    private void executeOrder(S_Order order, User user) {
        if (order == null) return;  // No order to execute

        try {
            String response;
            if ("buy".equalsIgnoreCase(order.getAction())) {
                response = upbitService.placeBuyOrder(user.getUpbitAccessKey(), user.getUpbitSecretKey(), "KRW-BTC", order.getAmount());
                System.out.println("Buy order placed at " + order.getTargetTime() + ": " + response);
            } else if ("sell".equalsIgnoreCase(order.getAction())) {
                response = upbitService.placeSellOrder(user.getUpbitAccessKey(), user.getUpbitSecretKey(), "KRW-BTC", order.getAmount());
                System.out.println("Sell order placed at " + order.getTargetTime() + ": " + response);
            } else {
                System.out.println("Invalid action for order.");
            }
        } catch (Exception e) {
            System.err.println("Failed to place " + order.getAction() + " order at " + order.getTargetTime() + ": " + e.getMessage());
        }
    }
}
