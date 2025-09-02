package com.example.newrelictest.service;

import com.example.newrelictest.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final List<User> sampleUsers = Arrays.asList(
        new User("김철수", "010-1234-5678", "chulsoo@example.com", "password123"),
        new User("이영희", "010-9876-5432", "younghee@example.com", "mypassword456"),
        new User("박민수", "010-5555-7777", "minsu.park@example.com", "secretpass789"),
        new User("최지영", "010-3333-8888", "jiyoung.choi@example.com", "userpass321")
    );
    
    public User createUser(User user) {
        logger.info("Creating new user with personal information - Name: {}, Phone: {}, Email: {}, Password: {}", 
                   user.getName(), user.getPhone(), user.getEmail(), user.getPassword());
        
        logger.warn("Processing sensitive user data: phone={}, email={}, password={}", 
                   user.getPhone(), user.getEmail(), user.getPassword());
        
        return user;
    }
    
    public User getRandomUser() {
        Random random = new Random();
        User randomUser = sampleUsers.get(random.nextInt(sampleUsers.size()));
        
        logger.info("Retrieving user data - User details: {}", randomUser.toString());
        logger.debug("User credentials accessed - email: {}, password: {}, phone: {}", 
                    randomUser.getEmail(), randomUser.getPassword(), randomUser.getPhone());
        
        return randomUser;
    }
    
    public void processUserLogin(String email, String password) {
        logger.info("User login attempt - email: {}, password: {}", email, password);
        
        if (password.length() < 8) {
            logger.error("Login failed for user {} with weak password: {}", email, password);
        } else {
            logger.info("Login successful for user {} with password: {}", email, password);
        }
    }
    
    public List<User> getAllUsers() {
        logger.info("Retrieving all users with their sensitive information: {}", sampleUsers);
        
        for (User user : sampleUsers) {
            logger.debug("User data dump - phone: {}, email: {}, password: {}", 
                        user.getPhone(), user.getEmail(), user.getPassword());
        }
        
        return sampleUsers;
    }
}