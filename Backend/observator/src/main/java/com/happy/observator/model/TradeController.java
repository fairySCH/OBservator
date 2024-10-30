package com.happy.observator.model;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.happy.observator.service.UpbitService;
import com.happy.observator.service.UserService;

@Controller
public class TradeController {

    private final UserService userService;
    private final UpbitService upbitService;

    public TradeController(UserService userService, UpbitService upbitService){
        this.userService = userService;
        this.upbitService = upbitService;
    }

    @GetMapping("/trade")
    public String showTradePage() {
        return "trade";  // Return the name of the template (trade.html)
    }

    @PostMapping("/trade")
    public String tradeBitcoin(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("price") String price, Model model) {
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
}
