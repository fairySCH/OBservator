package com.happy.observator.model;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.happy.observator.service.UserService;

@Controller
public class SignupController {
    
    private UserService userService;

    public SignupController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String email_local,
            @RequestParam(required = false) String email_domain,
            @RequestParam(required = false) String phone1,
            @RequestParam(required = false) String phone2,
            @RequestParam(required = false) String phone3, Model model) {
        try{
            // Combine email and phone number
            String email = email_local + "@" + email_domain;
            String phone = phone1 + "-" + phone2 + "-" + phone3;

            if (userService.userExists(username)) {
                model.addAttribute("errorMessage", "Username already taken. Please try another one.");
                return "signup";  // Render signup.html again with the error message
            }

            // Save user
            userService.saveUser(username, password, email, phone);

            // Add a success message
            model.addAttribute("successMessage", "User registered successfully! Please log in.");

            // Redirect to login with signup success message
            return "redirect:/login";
        } catch(IllegalArgumentException e){
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
        
    }
}