package com.english.api.order.service.impl;

import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.order.config.ExchangeRateProperties;
import com.english.api.order.model.enums.CurrencyType;
import com.english.api.order.service.ExchangeRateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "exchangeRates", key = "#from + '_' + #to")
    public BigDecimal getExchangeRate(CurrencyType from, CurrencyType to) {
        if (from == to) {
            return BigDecimal.ONE;
        }

        try {
            String url = properties.getApiUrl() + from.name();
            log.info("Fetching exchange rate from {} to {} using URL: {}", from, to, url);
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode rates = root.path("rates");
            
            if (rates.isMissingNode() || !rates.has(to.name())) {
                throw new ResourceInvalidException("Exchange rate not found for " + from + " to " + to);
            }
            
            BigDecimal rate = rates.path(to.name()).decimalValue();
            log.info("Exchange rate from {} to {}: {}", from, to, rate);
            return rate;
            
        } catch (Exception e) {
            log.error("Failed to fetch exchange rate from {} to {}: {}", from, to, e.getMessage(), e);
            throw new ResourceInvalidException("Failed to fetch exchange rate: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal convertAmount(Long amountCents, CurrencyType from, CurrencyType to) {
        if (from == to) {
            return BigDecimal.valueOf(amountCents);
        }
        
        BigDecimal rate = getExchangeRate(from, to);
        
        // Use database value as-is (no division by 100)
        BigDecimal amount = BigDecimal.valueOf(amountCents);
        BigDecimal convertedAmount = amount.multiply(rate);
        
        // Return with 2 decimal places precision
        BigDecimal convertedValue = convertedAmount.setScale(2, RoundingMode.HALF_UP);
        
        log.info("Converted {} {} to {} {} at rate {}", 
                amountCents, from, convertedValue, to, rate);
        
        return convertedValue;
    }
}
