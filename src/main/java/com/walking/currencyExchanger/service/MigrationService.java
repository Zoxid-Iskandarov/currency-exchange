package com.walking.currencyExchanger.service;

import lombok.AllArgsConstructor;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

@AllArgsConstructor
public class MigrationService {
    private final DataSource dataSource;

    public void migrate() {
        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();
    }
}
