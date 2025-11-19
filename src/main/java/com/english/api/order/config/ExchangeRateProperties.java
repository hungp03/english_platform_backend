package com.english.api.order.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "exchange-rate")
@Getter
@Setter
public class ExchangeRateProperties {
    private String apiUrl = "https://api.exchangerate-api.com/v4/latest/";
    private Integer cacheMinutes = 60;
}
