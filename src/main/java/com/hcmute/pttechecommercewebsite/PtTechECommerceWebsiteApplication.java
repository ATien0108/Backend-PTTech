package com.hcmute.pttechecommercewebsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PtTechECommerceWebsiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(PtTechECommerceWebsiteApplication.class, args);
    }

}
