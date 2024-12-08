package com.walking.currencyExchanger.repository;

import com.walking.currencyExchanger.converter.CurrencyConverter;
import com.walking.currencyExchanger.entity.Currency;
import lombok.AllArgsConstructor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class CurrencyRepository implements CrudRepository<Long, Currency> {
    private final DataSource dataSource;
    private final CurrencyConverter converter;

    @Override
    public Optional<Currency> findById(Long id) {
        var sql = """
                SELECT * FROM currency WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();

            return converter.convert(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении валюты по id = %s".formatted(id), e);
        }
    }

    public Optional<Currency> findByCode(String code) {
        var sql = """
                SELECT * FROM currency WHERE code = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, code);
            ResultSet rs = statement.executeQuery();

            return converter.convert(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении валюты по коду = %s".formatted(code), e);
        }
    }

    @Override
    public List<Currency> findAll() {
        var sql = """
                SELECT * FROM currency
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet rs = statement.executeQuery();

            return converter.convertMany(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении валют", e);
        }
    }

    @Override
    public Currency save(Currency entity) {
        var sql = """
                INSERT INTO currency (code, name, sign) VALUES (?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParameters(statement, entity);
            statement.executeUpdate();

            ResultSet gk = statement.getGeneratedKeys();
            if (gk.next()) {
                entity.setId(gk.getLong(1));
            }

            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении валюты", e);
        }
    }

    @Override
    public Currency update(Currency entity) {
        var sql = """
                UPDATE currency SET code = ?, name = ?, sign = ? WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement, entity);
            statement.setLong(4, entity.getId());

            statement.executeUpdate();

            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении валюты", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        var sql = """
                DELETE FROM currency WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении валюты", e);
        }
    }

    private void setParameters(PreparedStatement statement, Currency currency) throws SQLException {
        statement.setString(1, currency.getCode());
        statement.setString(2, currency.getName());
        statement.setString(3, currency.getSign());
    }
}
