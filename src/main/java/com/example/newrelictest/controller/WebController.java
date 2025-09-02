package com.example.newrelictest.controller;

import com.example.newrelictest.model.User;
import com.example.newrelictest.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WebController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/")
    public String home(Model model) {
        logger.info("Web home page accessed");
        model.addAttribute("message", "New Relic Java Test Application");
        model.addAttribute("description", "개인정보 로깅 및 New Relic APM 연동 테스트");
        return "index";
    }
    
    @GetMapping("/users")
    public String users(Model model) {
        logger.info("Web users page accessed - displaying sensitive user information");
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }
    
    @GetMapping("/random")
    public String randomUser(Model model) {
        logger.info("Web random user page accessed");
        User randomUser = userService.getRandomUser();
        model.addAttribute("user", randomUser);
        return "random";
    }
    
    @GetMapping("/login")
    public String loginForm() {
        logger.info("Login form page accessed");
        return "login";
    }
    
    @PostMapping("/login")
    public String processLogin(@RequestParam String email, @RequestParam String password, Model model) {
        logger.info("Web login form submitted with credentials - email: {}, password: {}", email, password);
        
        userService.processUserLogin(email, password);
        
        model.addAttribute("email", email);
        model.addAttribute("loginResult", "로그인 처리 완료 (로그 확인)");
        
        return "login-result";
    }
    
    @GetMapping("/create")
    public String createUserForm() {
        logger.info("Create user form page accessed");
        return "create";
    }
    
    @PostMapping("/create")
    public String processCreateUser(@RequestParam String name, 
                                   @RequestParam String phone,
                                   @RequestParam String email, 
                                   @RequestParam String password, 
                                   Model model) {
        
        logger.info("Web create user form submitted with personal data - name: {}, phone: {}, email: {}, password: {}", 
                   name, phone, email, password);
        
        User newUser = new User(name, phone, email, password);
        User createdUser = userService.createUser(newUser);
        
        model.addAttribute("user", createdUser);
        model.addAttribute("createResult", "사용자 생성 완료 (개인정보 로그 생성됨)");
        
        return "create-result";
    }
    
    @PostMapping("/submit-info")
    public String submitPersonalInfo(@RequestParam String name, 
                                   @RequestParam String phone,
                                   @RequestParam String email, 
                                   Model model) {
        
        // 로그 메시지에 개인정보를 포함하여 기록
        logger.info("Personal information submitted - message contains sensitive data: name={}, phone={}, email={}", 
                   name, phone, email);
        
        logger.warn("SENSITIVE DATA LOGGED - User personal information: name='{}', phone='{}', email='{}'", 
                   name, phone, email);
        
        logger.error("PRIVACY VIOLATION - Personal data exposed in logs: name:{}, phone:{}, email:{}", 
                    name, phone, email);
        
        // 마이크로서비스 API 호출 시뮬레이션 로그 (개인정보 포함)
        String customerNo = String.format("%010d", Math.abs(name.hashCode() % 10000000));
        String encodedEmail = java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8);
        
        logger.error("[GET] to [https://microservice.lgcomus-stg.lge.com/myaccount/process/v1/gsfs-call/sync-registered-product?customerNo={}&customerEmail={}&store=us] [MyLGProcessFeignClient#syncRegisteredProduct(String,String,String)]: {{\"exception\":\"NoSuchElementException\",\"errorCode\":\"MUL20-PRO-00\",\"message\":\"Something went wrong\",\"timestamp\":\"{}\",\"status\":500}}\n\tat feign.FeignException.clientErrorStatus(FeignException.java:243)\n\tat feign.codec.ErrorDecoder$Default.decode(ErrorDecoder.java:92)\n\tat feign.InvocationHandlerFactory$MethodHandler.invoke(MethodHandler.java:299)", 
                    customerNo, encodedEmail, java.time.Instant.now());
        
        logger.warn("[POST] to [https://api-gateway.lgcomus-stg.lge.com/user/profile/update] with payload: {{\"name\":\"{}\",\"phone\":\"{}\",\"email\":\"{}\",\"customerNo\":\"{}\"}} - Response: {{\"status\":\"failed\",\"error\":\"VALIDATION_ERROR\",\"details\":\"Invalid phone format\"}} at com.lg.user.ProfileServiceImpl.updateProfile(ProfileServiceImpl.java:156)", 
                    name, phone, email, customerNo);
        
        logger.info("External API call initiated: [MyLGProcessFeignClient] attempting to sync product data for customer {} with email {} and phone {}", 
                   customerNo, email, phone);
        
        logger.error("Database connection failed while processing user data: name='{}', email='{}', phone='{}' - SQLException: Connection timeout after 30000ms\n\tat com.mysql.cj.jdbc.ConnectionImpl.createNewIO(ConnectionImpl.java:836)\n\tat com.mysql.cj.jdbc.ConnectionImpl.<init>(ConnectionImpl.java:456)", 
                    name, email, phone);
        
        // 추가적인 로그 메시지들
        logger.info("Processing user data submission with details: {name: '{}', phone: '{}', email: '{}'}", 
                   name, phone, email);
        
        logger.debug("Raw personal data received: name=<{}>, phone=<{}>, email=<{}>", 
                    name, phone, email);
        
        model.addAttribute("name", name);
        model.addAttribute("phone", phone);
        model.addAttribute("email", email);
        model.addAttribute("submissionTime", java.time.LocalDateTime.now());
        
        return "submit-result";
    }
}