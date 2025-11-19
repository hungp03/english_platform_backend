package com.english.api.order.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "paypal")
public class PayPalProperties {
    private String clientId;
    private String clientSecret;
    private String baseUrl;
    private String successUrl;
    private String cancelUrl;
    private String brandName;
}
