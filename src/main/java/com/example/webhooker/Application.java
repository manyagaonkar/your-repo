package com.example.webhooker;

import com.example.webhooker.service.WebhookService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ApplicationRunner runner(WebhookService webhookService) {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) throws Exception {
                System.out.println(">>> Running startup flow...");
                webhookService.runFlowOnStartup();
                System.exit(0); // exit after running, so Tomcat doesn't stay open
            }
        };
    }
}
