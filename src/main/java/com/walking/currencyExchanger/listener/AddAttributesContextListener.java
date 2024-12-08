package com.walking.currencyExchanger.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walking.currencyExchanger.constant.ContextAttributeNames;
import com.walking.currencyExchanger.converter.CurrencyConverter;
import com.walking.currencyExchanger.converter.ExchangeRateConverter;
import com.walking.currencyExchanger.mapper.ExchangeRateDtoMapper;
import com.walking.currencyExchanger.repository.CurrencyRepository;
import com.walking.currencyExchanger.repository.ExchangeRateRepository;
import com.walking.currencyExchanger.service.MigrationService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

public class AddAttributesContextListener implements ServletContextListener {
    private static final String HIKARI_PROPERTIES_PATH = "/WEB-INF/classes/hikari.properties";

    private static final Logger log = LogManager.getLogger(AddAttributesContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        log.info("Запушена иницеализация атрибутов глобального контекста");

        var servletContext = event.getServletContext();

        var dataSource = hikariDataSource(servletContext);
        servletContext.setAttribute(ContextAttributeNames.DATASOURCE, dataSource);

        var currencyConverter = new CurrencyConverter();
        servletContext.setAttribute(ContextAttributeNames.CURRENCY_CONVERTER, currencyConverter);

        var exchangeRateConverter = new ExchangeRateConverter();
        servletContext.setAttribute(ContextAttributeNames.EXCHANGER_RATE_CONVERTER, exchangeRateConverter);

        var currencyRepository = new CurrencyRepository(dataSource, currencyConverter);
        servletContext.setAttribute(ContextAttributeNames.CURRENCY_REPOSITORY, currencyRepository);

        var exchangeRateRepository = new ExchangeRateRepository(dataSource, exchangeRateConverter);
        servletContext.setAttribute(ContextAttributeNames.EXCHANGE_RATE_REPOSITORY, exchangeRateRepository);

        var exchangeRateDtoMapper = new ExchangeRateDtoMapper(currencyRepository);
        servletContext.setAttribute(ContextAttributeNames.EXCHANGE_RATE_DTO_MAPPER, exchangeRateDtoMapper);

        var migrationService = new MigrationService(dataSource);
        servletContext.setAttribute(ContextAttributeNames.MIGRATION_SERVICE, migrationService);

        var objectMapper = new ObjectMapper();
        servletContext.setAttribute(ContextAttributeNames.OBJECT_MAPPER, objectMapper);

        log.info("Завершена инициализация атрибутов глобального контекста");
    }

    private DataSource hikariDataSource(ServletContext servletContext) {
        try (var propertiesInputStream = servletContext.getResourceAsStream(HIKARI_PROPERTIES_PATH)) {
            var hikariProperties = new Properties();
            hikariProperties.load(propertiesInputStream);

            var configuration = new HikariConfig(hikariProperties);

            return new HikariDataSource(configuration);
        } catch (IOException e) {
            log.error("Невозможно загрузить конфигурацию для HikariCP", e);

            throw new RuntimeException(e);
        }
    }
}
