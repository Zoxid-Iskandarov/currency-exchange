package com.walking.currencyExchanger.dto;

import com.walking.currencyExchanger.entity.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExchangeRateDto {
    private Long id;
    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;
}
