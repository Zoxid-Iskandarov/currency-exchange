package com.walking.currencyExchanger.repository;

import com.walking.currencyExchanger.converter.ExchangeRateConverter;
import com.walking.currencyExchanger.entity.ExchangeRate;
import lombok.AllArgsConstructor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class ExchangeRateRepository implements CrudRepository<Long, ExchangeRate> {
    private DataSource dataSource;
    private ExchangeRateConverter converter;

    @Override
    public Optional<ExchangeRate> findById(Long id) {
        var sql = """
                SELECT * FROM exchange_rate WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();

            return converter.convert(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении обменного курса по id = %s".formatted(id), e);
        }
    }

    public Optional<ExchangeRate> findByCodes(String baseCurrencyCode, String targetCurrencyCode) {
        var sql = """
                SELECT exchange_rate.id, exchange_rate.base_currency_id, exchange_rate.target_currency_id, exchange_rate.rate
                FROM exchange_rate
                JOIN public.currency c1 on c1.id = exchange_rate.base_currency_id
                JOIN public.currency c2 on c2.id = exchange_rate.target_currency_id
                WHERE c1.code = ? AND c2.code = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);

            ResultSet rs = statement.executeQuery();

            return converter.convert(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получени обменного курса по кодам %s%s".formatted(baseCurrencyCode, targetCurrencyCode), e);
        }
    }

    @Override
    public List<ExchangeRate> findAll() {
        var sql = """
                SELECT * FROM exchange_rate
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet rs = statement.executeQuery();

            return converter.convertMany(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении обменных курсов", e);
        }
    }

    @Override
    public ExchangeRate save(ExchangeRate entity) {
        var sql = """
                INSERT INTO exchange_rate (base_currency_id, target_currency_id, rate) VALUES (?, ?, ?)
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
            throw new RuntimeException("Ошибка при сохранении обменного курса", e);
        }
    }

    @Override
    public ExchangeRate update(ExchangeRate entity) {
        var sql = """
                UPDATE exchange_rate SET base_currency_id = ?, target_currency_id = ?, rate = ? WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement, entity);
            statement.setLong(4, entity.getId());
            statement.executeUpdate();

            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении обменного курса", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        var sql = """
                DELETE FROM exchange_rate WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении обменного курса", e);
        }
    }

    private void setParameters(PreparedStatement statement, ExchangeRate exchangeRate) throws SQLException {
        statement.setLong(1, exchangeRate.getBaseCurrencyId());
        statement.setLong(2, exchangeRate.getTargetCurrencyId());
        statement.setBigDecimal(3, exchangeRate.getRate());
    }
}
