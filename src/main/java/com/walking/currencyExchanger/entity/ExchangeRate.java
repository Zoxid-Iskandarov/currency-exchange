package com.walking.currencyExchanger.entity;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {
    private Long id;
    private Long baseCurrencyId;
    private Currency baseCurrency;
    private Long targetCurrencyId;
    private Currency targetCurrency;
    private BigDecimal rate;
}
