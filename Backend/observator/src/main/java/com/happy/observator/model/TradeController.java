package com.happy.observator.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.happy.observator.service.UpbitService;
import com.happy.observator.service.UserService;
import com.happy.observator.repository.OrderRepositary;
import com.happy.observator.repository.UserRepositary;

@Controller
public class TradeController {

    private final UserService userService;
    private final UpbitService upbitService;
    private final UserRepositary userRepository;
    private final OrderRepositary orderRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<LocalTime, S_Order> scheduledOrders = new ConcurrentHashMap<>();

    @Autowired
    public TradeController(UserService userService, UpbitService upbitService, OrderRepositary orderRepositary, UserRepositary userRepository){
        this.userService = userService;
        this.upbitService = upbitService;
        this.orderRepository = orderRepositary;
        this.userRepository = userRepository;
    }

    @GetMapping("/trade")
    public String showTradePage() {
        return "trade";  // Return the name of the template (trade.html)
    }

    @PostMapping("/buy")
    public String buyBitcoin(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("price") String price, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        try {
            String response = upbitService.placeBuyOrder(user.getUpbitAccessKey(), user.getUpbitSecretKey(), "KRW-BTC", price);
            model.addAttribute("successMessage", "Buy order placed successfully: " + response);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to place buy order: " + e.getMessage());
        }

        return "trade";  // Render the same page with success or error message
    }

    @PostMapping("/sell")
    public String sellBitcoin(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("volume") String volume, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        try {
            String response = upbitService.placeSellOrder(user.getUpbitAccessKey(), user.getUpbitSecretKey(), "KRW-BTC", volume);
            model.addAttribute("successMessage", "Sell order placed successfully: " + response);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to place sell order: " + e.getMessage());
        }

        return "trade";  // Render the same page with success or error message
    }

    @PostMapping("/scheduleBuy")
    public String scheduleBuyBitcoin(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("price") String price, @RequestParam String targetTime, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        LocalTime target = LocalTime.parse(targetTime);
        LocalTime now = LocalTime.now();
        long delay = Duration.between(now, target).getSeconds();
        
        if (delay < 0) {
            model.addAttribute("errorMessage", "Target time has already passed.");
            return "trade";
        }

        scheduler.schedule(() -> {
            try {
                String response = upbitService.placeBuyOrder(user.getUpbitAccessKey(), user.getUpbitSecretKey(), "KRW-BTC", price);
                model.addAttribute("successMessage", "Buy order placed successfully at" + targetTime + ": " + response);
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Failed to place buy order: " + e.getMessage());
            }
        }, delay, TimeUnit.SECONDS);

        return "trade";  // Render the same page with success or error message
    }

    @PostMapping("/scheduleSell")
    public String scheduleSellBitcoin(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("volume") String volume, @RequestParam String targetTime, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        LocalTime target = LocalTime.parse(targetTime);
        LocalTime now = LocalTime.now();
        long delay = Duration.between(now, target).getSeconds();

        if (delay < 0) {
            model.addAttribute("errorMessage", "Target time has already passed.");
            return "trade";
        }

        scheduler.schedule(() -> {
            try {
                String response = upbitService.placeSellOrder(user.getUpbitAccessKey(), user.getUpbitSecretKey(), "KRW-BTC", volume);
                model.addAttribute("successMessage", "Sell order placed successfully at" + targetTime + ": " + response);
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Failed to place sell order: " + e.getMessage());
            }
        }, delay, TimeUnit.SECONDS);

        return "trade";  // Render the same page with success or error message
    }

    @PostMapping("/scheduleOrder")
    public String scheduleOrderBitcoin(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String action, @RequestParam String amount, @RequestParam String targetTime, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create a unique ID for the order
        String orderId = UUID.randomUUID().toString();
        
        // Check if there's an existing order for the target time and delete it
        LocalTime target = LocalTime.parse(targetTime);
        Optional<S_Order> existingOrder = orderRepository.findByUserIdAndTargetTime(user.getId(), target);
        if (existingOrder.isPresent()) {
                orderRepository.delete(existingOrder.get());
                System.out.println("Deleted existing order: " + existingOrder);
        }
        // Save new order
        S_Order order = new S_Order(orderId, user.getId(), action, amount, "KRW-BTC", target);
        orderRepository.save(order);

        LocalTime now = LocalTime.now();
        long delay = Duration.between(now, target).getSeconds();
        
        if (delay < 0) {
            model.addAttribute("errorMessage", "Target time has already passed.");
            return "trade";
        }

        scheduler.schedule(() -> executeOrder(order, user), delay, TimeUnit.SECONDS);
        model.addAttribute("successMessage", action + " order scheduled for " + targetTime);
        model.addAttribute("scheduledOrders", scheduledOrders);  // Add orders to the model

        return "trade";  // Render the same page with success or error message
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

    @GetMapping("/reloadScheduledOrders")
    @ResponseBody
    public List<S_Order> getScheduledOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findAll().stream()
            .filter(order -> order.getUserId() == user.getId())
            .toList();  // Return only the current user's scheduled orders
    }
}
