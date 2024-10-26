package com.happy.observator.model;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.happy.observator.service.UserService;

@Controller
public class ProfileController {
    
    private final UserService userService;

    public ProfileController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
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
