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
        
        // 마이크로서비스 API 호출 시뮬레이션 로그
        String customerNo = String.format("%010d", Math.abs(randomUser.getName().hashCode() % 10000000));
        String encodedEmail = java.net.URLEncoder.encode(randomUser.getEmail(), java.nio.charset.StandardCharsets.UTF_8);
        
        logger.error("[GET] to [https://payment-service.lgcomus-stg.lge.com/api/v2/payment-history?email={}&phone={}] [PaymentServiceClient#getPaymentHistory(String,String)]: {{\"exception\":\"ConnectTimeoutException\",\"errorCode\":\"PAY-TIMEOUT-01\",\"message\":\"Connection timeout to payment service\",\"timestamp\":\"{}\",\"status\":503}}\n\tat org.springframework.web.client.ResourceAccessException: I/O error on GET request\n\tat org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:785)", 
                    encodedEmail, randomUser.getPhone(), java.time.Instant.now());
        
        return randomUser;
    }
    
    public void processUserLogin(String email, String password) {
        logger.info("User login attempt - email: {}, password: {}", email, password);
        
        // 인증 서비스 API 호출 시뮬레이션
        String encodedEmail = java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8);
        logger.error("[POST] to [https://auth-service.lgcomus-stg.lge.com/oauth/token] [AuthServiceClient#authenticate(String,String)] with credentials: {{\"username\":\"{}\",\"password\":\"{}\"}} - Response: {{\"exception\":\"InvalidCredentialsException\",\"errorCode\":\"AUTH-401-01\",\"message\":\"Invalid username or password\",\"timestamp\":\"{}\",\"status\":401}}\n\tat com.lg.auth.AuthenticationException: Authentication failed\n\tat com.lg.auth.service.AuthService.authenticate(AuthService.java:98)", 
                    email, password, java.time.Instant.now());
        
        if (password.length() < 8) {
            logger.error("Login failed for user {} with weak password: {}", email, password);
            
            logger.warn("[GET] to [https://user-service.lgcomus-stg.lge.com/api/user/security-questions?email={}] [UserSecurityClient#getSecurityQuestions(String)]: User security validation failed for email: {} with attempted password: {}", 
                       encodedEmail, email, password);
        } else {
            logger.info("Login successful for user {} with password: {}", email, password);
            
            logger.info("[GET] to [https://profile-service.lgcomus-stg.lge.com/api/v1/user/profile?email={}] [ProfileServiceClient#getUserProfile(String)] - Retrieved profile data for user: {} (email: {}, session created)", 
                       encodedEmail, email, email);
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