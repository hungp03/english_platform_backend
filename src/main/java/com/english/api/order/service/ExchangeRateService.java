package com.english.api.order.service;

import com.english.api.order.model.enums.CurrencyType;

import java.math.BigDecimal;

public interface ExchangeRateService {
    BigDecimal getExchangeRate(CurrencyType from, CurrencyType to);
    
    BigDecimal convertAmount(Long amountCents, CurrencyType from, CurrencyType to);
}
