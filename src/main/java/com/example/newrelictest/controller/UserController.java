package com.example.newrelictest.controller;

import com.example.newrelictest.model.User;
import com.example.newrelictest.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        logger.info("API call to create user with sensitive data - phone: {}, email: {}, password: {}", 
                   user.getPhone(), user.getEmail(), user.getPassword());
        
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }
    
    @GetMapping("/random")
    public ResponseEntity<User> getRandomUser() {
        logger.info("API call to get random user with personal information");
        User user = userService.getRandomUser();
        
        logger.info("Returning user data - phone: {}, email: {}, password: {}", 
                   user.getPhone(), user.getEmail(), user.getPassword());
        
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        
        logger.info("Login API called with credentials - email: {}, password: {}", email, password);
        
        userService.processUserLogin(email, password);
        
        return ResponseEntity.ok("Login processed");
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("API call to retrieve all users with sensitive information");
        List<User> users = userService.getAllUsers();
        
        logger.warn("Exposing all user data including passwords and phone numbers: {}", users);
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("Health check endpoint called");
        return ResponseEntity.ok("Application is running");
    }
}