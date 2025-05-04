package com.hcmute.pttechecommercewebsite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // Các API public không cần xác thực
                        .requestMatchers(HttpMethod.GET, "/api/ad-images", "/api/ad-images/no-delete", "/api/ad-images/{id}", "/api/ad-images/search",
                                "/api/brands", "/api/brands/no-delete", "/api/brands/{id}", "/api/brands/search",
                                "/api/categories", "/api/categories/no-delete", "/api/categories/{id}", "/api/categories/search", "/api/categories/parent/{parentCategoryId}",
                                "/api/contacts", "/api/contacts/no-delete", "/api/contacts/{id}",
                                "/api/discount-codes", "/api/discount-codes/no-delete", "/api/discount-codes/{id}", "/api/discount-codes/search",
                                "/api/policies", "/api/policies/no-delete", "/api/policies/{id}", "/api/policies/search",
                                "/api/products", "/api/products/active", "/api/products/search", "/api/products/{id}", "/api/products/by-product-id/{productId}", "/api/users/verify",
                                "/api/reviews", "/api/reviews/{id}", "/api/reviews/product/{productId}", "/videos/**", "/images/**",
                                "/api/qas/product/{productId}", "/api/qas/user/{userId}", "/api/qas", "/api/users/{id}")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/register", "/api/users/login", "/api/users/forgot-password",
                                "/api/users/reset-password", "/api/users/google-login", "/api/users/facebook-login", "/api/users/subscribe")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/users/{id}", "/api/orders/{id}", "/api/carts/user/{userId}", "/api/orders/user/{userId}")
                        .hasAnyRole("ADMIN", "MANAGER", "MARKETING", "INVENTORY_MANAGER", "CUSTOMER_SUPPORT", "CUSTOMER")

                        .requestMatchers(HttpMethod.GET, "/api/ad-images/export-excel", "/api/discount-codes/export-excel", "/api/statistics",
                                "/api/statistics/{id}", "/api/statistics/export-excel", "/api/inventories", "/api/inventories/filter", "/api/inventories/sorted",
                                "/api/inventories/sorted-by-quantity", "/api/inventories/{id}", "/api/inventories/export-excel",
                                "/api/brands/export-excel", "/api/categories/export-excel", "/api/contacts/export-excel", "/api/policies/export-excel",
                                "/api/reviews/user/{userId}", "/api/reviews/product/{productId}", "/api/reviews/order/{orderId}", "/api/carts",
                                "/api/carts/{cartId}", "/api/orders", "/api/orders/order-id/{orderId}",
                                "/api/orders/top-10-items", "/api/orders/top-10-price", "/api/orders/product/{productId}",
                                "/api/orders/export-excel", "/api/products/inactive","/api/products/top-selling", "/api/products/top-rated",
                                "/api/products/low-stock", "/api/products/export-excel", "/api/users", "/api/users/search?username={username}",
                                "/api/users/export-excel")
                        .hasAnyRole("ADMIN", "MANAGER", "MARKETING", "INVENTORY_MANAGER", "CUSTOMER_SUPPORT")

                        // Phân quyền cho Role MARKETING
                        .requestMatchers(HttpMethod.POST, "/api/ad-images", "/api/ad-images/schedule-create", "/api/ad-images/upload-images",
                        "/api/discount-codes", "/api/discount-codes/schedule-create")
                        .hasAnyRole("ADMIN", "MANAGER", "MARKETING")
                        .requestMatchers(HttpMethod.PUT, "/api/ad-images/{id}", "/api/ad-images/hide/{id}", "/api/ad-images/show/{id}",
                        "/api/discount-codes/{id}", "/api/discount-codes/hide/{id}", "/api/discount-codes/show/{id}")
                        .hasAnyRole("ADMIN", "MANAGER", "MARKETING")
                        .requestMatchers(HttpMethod.DELETE, "/api/ad-images/{id}", "/api/ad-images/delete-image/{id}", "/api/discount-codes/{id}")
                        .hasAnyRole("ADMIN", "MANAGER", "MARKETING")

                        // Phân quyền cho Role Inventory Manager
                        .requestMatchers(HttpMethod.POST, "/api/inventories", "/api/products", "/api/products/schedule",
                                "/api/products/upload-image/{productId}", "/api/products/upload-video/{productId}")
                        .hasAnyRole("ADMIN", "MANAGER", "INVENTORY_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/products/{id}", "/api/products/update-price/{productId}",
                                "/api/products/hide/{id}", "/api/products/show/{id}")
                        .hasAnyRole("ADMIN", "MANAGER", "INVENTORY_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/inventories/{id}", "/api/products/{id}",
                                "/api/products/delete-image/{productId}", "/api/products/delete-video/{productId}")
                        .hasAnyRole("ADMIN", "MANAGER", "INVENTORY_MANAGER")

                        // Phân quyền cho Role Customer Support
                        .requestMatchers(HttpMethod.POST, "/api/brands", "/api/brands/schedule-create", "/api/brands/upload-images",
                                "/api/categories", "/api/categories/schedule-create", "/api/categories/upload-images",
                                "/api/contacts", "/api/contacts/schedule-create", "/api/policies", "/api/policies/schedule-create",
                                "/api/reviews/reply/{id}", "/api/users/send-notification", "/api/qas/{qaId}/answer", "/api/orders/{orderId}/complete-return")
                        .hasAnyRole("ADMIN", "MANAGER", "CUSTOMER_SUPPORT")
                        .requestMatchers(HttpMethod.PUT, "/api/brands/{id}", "/api/brands/hide/{id}", "/api/brands/show/{id}",
                                "/api/categories/{id}", "/api/categories/hide/{id}", "/api/categories/show/{id}",
                                "/api/contacts/{id}", "/api/contacts/hide/{id}", "/api/contacts/show/{id}",
                                "/api/policies/{id}", "/api/policies/hide/{id}", "/api/policies/show/{id}",
                                "/api/orders/{orderId}", "/api/users/block/{id}?blockReason={blockReason}", "/api/users/unblock/{id}",
                                "/api/qas/{qaId}/answer/{questionId}")
                        .hasAnyRole("ADMIN", "MANAGER", "CUSTOMER_SUPPORT")
                        .requestMatchers(HttpMethod.DELETE, "/api/brands/{id}", "/api/brands/delete-image/{id}", "/api/categories/{id}",
                                "/api/categories/delete-image/{id}", "/api/contacts/{id}", "/api/policies/{id}", "/api/qas/{qaId}/answer/{questionId}")
                        .hasAnyRole("ADMIN", "MANAGER", "CUSTOMER_SUPPORT")

                        // Phân quyền cho Role Customer
                        .requestMatchers(HttpMethod.GET, "/api/reviews/user/{userId}", "/api/reviews/order/{orderId}", "/api/orders/order-id/{orderId}", "/api/orders/product/{productId}")
                        .hasAnyRole("ADMIN", "MANAGER", "CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/carts/{cartId}/items", "/api/reviews", "/api/orders",
                                "/api/orders/cancel/{orderId}", "/api/orders/{orderId}/request-return", "/api/qas",
                                "/api/orders/vnpay/{orderId}", "/api/orders/vnpay/return", "/api/users/upload-avatar/{userId}")
                        .hasAnyRole("ADMIN", "MANAGER", "CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "api/carts/{cartId}/increase/{productId}/{variantId} ",
                                "/api/carts/{cartId}/decrease/{productId}/{variantId}",
                                "/api/carts/{cartId}/change-variant/{productId}/{oldVariantId}/{newVariantId}",
                                "/api/reviews/{id}", "/api/users/{id}",
                                "/api/users/delete-avatar/{userId}")
                        .hasAnyRole("ADMIN", "MANAGER", "CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE,"/api/carts/{cartId}/items/{productId}/{variantId}", "/api/reviews/{id}", "/api/users/{id}")
                        .hasAnyRole("ADMIN", "MANAGER", "CUSTOMER")

                        // Phân quyền cho Role Admin
                        .requestMatchers(HttpMethod.POST, "/api/users")
                        .hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/qas/{id}")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/qas/{id}")
                        .hasRole("ADMIN")

                        // Nếu không match với bất kỳ quy tắc nào thì yêu cầu xác thực
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(withDefaults())
                .oauth2Login(oauth2 -> oauth2
                        .clientRegistrationRepository(clientRegistrationRepository()))
                .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration googleClientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("99750422196-cp0va3lft8pindu7u759jj0dg46jbkkt.apps.googleusercontent.com")
                .clientSecret("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")
                .scope("email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .redirectUri("http://localhost:8081/login/oauth2/code/google")
                .clientName("Google")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();

        return new InMemoryClientRegistrationRepository(googleClientRegistration);
    }
}
