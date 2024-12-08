package com.walking.currencyExchanger.converter;

import com.walking.currencyExchanger.entity.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyConverter implements ResultSetConverter<Optional<Currency>> {
    @Override
    public Optional<Currency> convert(ResultSet rs) throws SQLException {
        return rs.next() ? Optional.of(mapToRow(rs)) : Optional.empty();
    }

    public List<Currency> convertMany(ResultSet rs) throws SQLException {
        var currencies = new ArrayList<Currency>();

        while (rs.next()) {
            currencies.add(mapToRow(rs));
        }

        return currencies;
    }

    private Currency mapToRow(ResultSet rs) throws SQLException {
        return Currency.builder()
                .id(rs.getLong("id"))
                .code(rs.getString("code"))
                .name(rs.getString("name"))
                .sign(rs.getString("sign"))
                .build();
    }
}
