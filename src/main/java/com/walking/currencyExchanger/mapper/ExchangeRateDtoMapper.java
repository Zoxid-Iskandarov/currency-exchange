package com.walking.currencyExchanger.mapper;

import com.walking.currencyExchanger.dto.ExchangeRateDto;
import com.walking.currencyExchanger.entity.ExchangeRate;
import com.walking.currencyExchanger.repository.CurrencyRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExchangeRateDtoMapper implements Mapper<ExchangeRate, ExchangeRateDto> {
    private CurrencyRepository repository;

    @Override
    public ExchangeRateDto map(ExchangeRate from) {
        return ExchangeRateDto.builder()
                .id(from.getId())
                .baseCurrency(repository.findById(from.getBaseCurrencyId()).get())
                .targetCurrency(repository.findById(from.getTargetCurrencyId()).get())
                .rate(from.getRate())
                .build();
    }
}
