package com.happy.observator.model;

import java.util.List;

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
                                @RequestParam String upbitSecretKey,
                                @RequestParam String email) {
        String username = userDetails.getUsername();
        userService.updateUserInfo(username, upbitAccessKey, upbitSecretKey, email);
        return "redirect:/profile";
    }

    @RestController
    @RequestMapping("/api/balances")
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
}
