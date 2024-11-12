package com.happy.observator.model;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String email_local,
            @RequestParam(required = false) String email_domain,
            @RequestParam(required = false) String email_domain_custom,
            @RequestParam(required = false) String phone1,
            @RequestParam(required = false) String phone2,
            @RequestParam(required = false) String phone3,
            @RequestParam(required = false) String email_opt_in,
            Model model) {

        try {
            String email = email_local + "@";
            if ("custom".equals(email_domain)) {
                email += email_domain_custom;
            } else {
                email += email_domain;
            }

            String phone = phone1 + "-" + phone2 + "-" + phone3;
            if (userService.userExists(username)) {
                model.addAttribute("errorMessage", "Username already taken. Please try another one.");
                return "signup";
            }

            int emailAgreed = (email_opt_in != null) ? 1 : 0;
            userService.saveUser(username, password, email, phone, emailAgreed);
            model.addAttribute("successMessage", "User registered successfully! Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }
}
