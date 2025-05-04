package com.hcmute.pttechecommercewebsite.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {

    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        // MongoDB
        System.setProperty("MONGODB_URI", dotenv.get("MONGODB_URI"));
        System.setProperty("MONGODB_DATABASE", dotenv.get("MONGODB_DATABASE"));

        // Email
        System.setProperty("MAIL_USERNAME", dotenv.get("MAIL_USERNAME"));
        System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));

        // VNPay
        System.setProperty("VNPAY_TMN_CODE", dotenv.get("VNPAY_TMN_CODE"));
        System.setProperty("VNPAY_HASH_SECRET", dotenv.get("VNPAY_HASH_SECRET"));

        // Google OAuth
        System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
        System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));

        // Facebook OAuth
        System.setProperty("FACEBOOK_CLIENT_ID", dotenv.get("FACEBOOK_CLIENT_ID"));
        System.setProperty("FACEBOOK_CLIENT_SECRET", dotenv.get("FACEBOOK_CLIENT_SECRET"));
    }
}