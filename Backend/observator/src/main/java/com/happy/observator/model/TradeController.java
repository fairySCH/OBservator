package com.happy.observator.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happy.observator.service.UpbitService;
import com.happy.observator.service.UserService;
import com.happy.observator.Upbit.UpbitBalance;
import com.happy.observator.repository.OrderRepositary;

@Controller
public class TradeController {

    private final UserService userService;
    private final UpbitService upbitService;
    private final OrderRepositary orderRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<LocalTime, S_Order> scheduledOrders = new ConcurrentHashMap<>();
    private Process pythonProcess;
    private boolean isAutoTrading = false;

    public TradeController(UserService userService, UpbitService upbitService, OrderRepositary orderRepositary){
        this.userService = userService;
        this.upbitService = upbitService;
        this.orderRepository = orderRepositary;
    }

    @GetMapping("/trade")
    public String showTradePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // Check if keys are present
        boolean hasKeys = user.getUpbitAccessKey() != null && user.getUpbitSecretKey() != null;

        if (hasKeys){
            try {
                List<UpbitBalance> balances = upbitService.getBalances(user.getUpbitAccessKey(), user.getUpbitSecretKey());
                model.addAttribute("balances", balances);
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Failed to fetch balance: " + e.getMessage());
            }
        } else {
            model.addAttribute("errorMessage", "Please input your Upbit API keys to view balance.");
        }
        
        model.addAttribute("user", user);
        model.addAttribute("hasKeys", hasKeys);
        model.addAttribute("isAutoTrading", isAutoTrading);

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

        return "redirect:/trade";  // Render the same page with success or error message
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

        return "redirect:/trade";  // Render the same page with success or error message
    }

    @PostMapping("/order")
    public String orderBitcoin(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String action, @RequestParam String amount, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        try {
            String response;
            if ("buy".equalsIgnoreCase(action)) {
                response = upbitService.placeBuyOrder(user.getUpbitAccessKey(), user.getUpbitSecretKey(), "KRW-BTC", amount);
                model.addAttribute("successMessage", "Buy order placed successfully: " + response);
            } else if ("sell".equalsIgnoreCase(action)) {
                response = upbitService.placeSellOrder(user.getUpbitAccessKey(), user.getUpbitSecretKey(), "KRW-BTC", amount);
                model.addAttribute("successMessage", "Sell order placed successfully: " + response);
            } else {
                model.addAttribute("errorMessage", "Invalid action. Please specify 'buy' or 'sell'.");
                return "redirect:/trade";
        }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to place sell order: " + e.getMessage());
        }

        return "redirect:/trade";  // Render the same page with success or error message
    }

    /* all in schedule Order
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

        return "redirect:/trade";  // Render the same page with success or error message
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

        return "redirect:/trade";  // Render the same page with success or error message
    }
    */
    @PostMapping("/scheduleOrder")
    public String scheduleOrderBitcoin(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String action, @RequestParam String amount, @RequestParam String targetTime, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create a unique ID for the order
        String orderId = UUID.randomUUID().toString();

        // Check if there's an existing order for the target time and delete it
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        LocalTime target = LocalTime.parse(targetTime, formatter);
        Optional<S_Order> existingOrder = orderRepository.findByUserIdAndTargetTime(user.getId(), target);
        if (existingOrder.isPresent()) {
                orderRepository.delete(existingOrder.get());
                System.out.println("Deleted existing order: " + existingOrder);
        }

        // Save new order
        S_Order order = new S_Order(orderId, user.getId(), action, amount, "KRW-BTC", target);
        orderRepository.save(order);

        LocalTime now = LocalTime.now();
        long delay = Duration.between(now, target).toMillis();
        
        if (delay < 0) {
            model.addAttribute("errorMessage", "Target time has already passed.");
            return "redirect:/trade";
        }

        scheduler.schedule(() -> executeOrder(order, user), delay, TimeUnit.MILLISECONDS);
        model.addAttribute("successMessage", action + " order scheduled for " + targetTime);
        model.addAttribute("scheduledOrders", scheduledOrders);  // Add orders to the model

        return "redirect:/trade";  // Render the same page with success or error message
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

    @RestController
    @RequestMapping("/api/tradebalances")
    public class BalanceRestController {

        @Autowired
        private UserService userService;

        @Autowired
        private UpbitService upbitService;

        @GetMapping
        public ResponseEntity<?> getBalances(@AuthenticationPrincipal UserDetails userDetails) {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

            // Check if keys are present
            if (user.getUpbitAccessKey() != null && user.getUpbitSecretKey() != null) {
                try {
                    List<UpbitBalance> balances = upbitService.getBalances(user.getUpbitAccessKey(), user.getUpbitSecretKey());
                    //System.out.println(balances);
                    return ResponseEntity.ok(balances);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch balances: " + e.getMessage());
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("API keys are missing.");
            }
        }
    }

    @PostMapping("/start")
    private String startTrade(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("ThresholdLevel") float thresholdLevel, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        float threshold = thresholdLevel / 10000;
        System.out.println("Received Threshold level: " + threshold + ". User ID: " + user.getId());

        try {
            // Python 스크립트를 실행, user ID와 threshold를 전달
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python3",
                "/home/ubuntu/project/OBservator/Backend/observator/python/test.py",
                String.valueOf(threshold),
                String.valueOf(user.getId()) // User ID 추가
            );
            processBuilder.redirectErrorStream(true);
            pythonProcess = processBuilder.start();

            // 상태 업데이트
            isAutoTrading = true;

            // Python 출력 로그 읽기
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[Python Log]: " + line);
                    }
                } catch (Exception e) {
                    System.err.println("Error reading Python script output: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            System.err.println("Failed to start Python script: " + e.getMessage());
        }

        model.addAttribute("isAutoTrading", isAutoTrading); // 상태 전달
        return "redirect:/trade";
    }


    @PostMapping("/end")
    private String endTrade(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("End Trade User ID: " + user.getId());

        if (pythonProcess != null && pythonProcess.isAlive()) {
            pythonProcess.destroy();
            System.out.println("Python script stopped successfully.");
            isAutoTrading = false;
        } else {
            System.out.println("No Python script is currently running.");
        }

        model.addAttribute("isAutoTrading", isAutoTrading); // 상태 전달
        return "redirect:/trade";
    }
}
