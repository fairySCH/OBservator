package com.happy.observator.model;

import com.happy.observator.repository.UserRepositary;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

@CrossOrigin(origins = "https://observator.ngrok.dev") 
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepositary userRepository;

    @PostMapping("/check-username")
    public ResponseEntity<UsernameResponse> checkUsername(@RequestBody UsernameRequest usernameRequest) {
        String username = usernameRequest.getUsername();

        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(new UsernameResponse(false, "Username is required"));
        }

        boolean exists = userRepository.existsByUsername(username);
        return ResponseEntity.ok(new UsernameResponse(exists, exists ? "이미 등록된 아이디입니다." : "사용할 수 있는 아이디입니다."));
    }

    public static class UsernameRequest {
        private String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static class UsernameResponse {
        private boolean exists;
        private String message;

        public UsernameResponse(boolean exists, String message) {
            this.exists = exists;
            this.message = message;
        }

        public boolean isExists() {
            return exists;
        }

        public String getMessage() {
            return message;
        }
    }
}