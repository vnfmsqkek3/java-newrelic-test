package com.example.newrelictest.config;

import com.newrelic.api.agent.NewRelic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NewRelicConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(NewRelicConfig.class);
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application started - NewRelic agent initialized");
        
        NewRelic.addCustomParameters(java.util.Map.of(
            "app.version", "1.0.0",
            "app.environment", "development",
            "sensitive.data.logging", "enabled"
        ));
        
        logger.info("NewRelic custom attributes set with sensitive data logging enabled");
    }
}