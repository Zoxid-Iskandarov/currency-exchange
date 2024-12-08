package com.walking.currencyExchanger.converter;

import com.walking.currencyExchanger.entity.ExchangeRate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateConverter implements ResultSetConverter<Optional<ExchangeRate>> {
    @Override
    public Optional<ExchangeRate> convert(ResultSet rs) throws SQLException {
        return rs.next() ? Optional.of(mapToRow(rs)) : Optional.empty();
    }

    public List<ExchangeRate> convertMany(ResultSet rs) throws SQLException {
        var exchangeRates = new ArrayList<ExchangeRate>();

        while (rs.next()) {
            exchangeRates.add(mapToRow(rs));
        }

        return exchangeRates;
    }

    private ExchangeRate mapToRow(ResultSet rs) throws SQLException {
        return ExchangeRate.builder()
                .id(rs.getLong("id"))
                .baseCurrencyId(rs.getLong("base_currency_id"))
                .targetCurrencyId(rs.getLong("target_currency_id"))
                .rate(rs.getBigDecimal("rate"))
                .build();
    }
}
