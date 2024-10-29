package com.happy.observator.model;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.happy.observator.Upbit.UpbitBalance;
import com.happy.observator.service.UpbitService;
import com.happy.observator.service.UserService;

@Controller
public class ProfileController {
    
    private final UserService userService;
    private final UpbitService upbitService;

    public ProfileController(UserService userService, UpbitService upbitService){
        this.userService = userService;
        this.upbitService = upbitService;
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
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
        
        return "profile";  // This resolves to profile.html in the templates folder
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String upbitAccessKey,
                                @RequestParam String upbitSecretKey) {
        String username = userDetails.getUsername();
        userService.updateUpbitKeys(username, upbitAccessKey, upbitSecretKey);
        return "redirect:/profile?success=true";
    }
}
