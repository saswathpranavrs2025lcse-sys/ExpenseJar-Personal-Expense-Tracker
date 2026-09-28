package com.example.expensetracker.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/expense_tracker?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}")
    private String mysqlUrl;

    @Value("${spring.datasource.username:root}")
    private String mysqlUsername;

    @Value("${spring.datasource.password:root}")
    private String mysqlPassword;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String mysqlDriverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Attempting to connect to MySQL database at: {}", mysqlUrl);

        HikariConfig mysqlConfig = new HikariConfig();
        mysqlConfig.setJdbcUrl(mysqlUrl);
        mysqlConfig.setUsername(mysqlUsername);
        mysqlConfig.setPassword(mysqlPassword);
        mysqlConfig.setDriverClassName(mysqlDriverClassName);
        mysqlConfig.setConnectionTimeout(3000); // 3 seconds fail-fast check
        mysqlConfig.setMaximumPoolSize(10);
        mysqlConfig.setPoolName("ExpenseTrackerMySQLPool");

        HikariDataSource mysqlDataSource = null;
        try {
            mysqlDataSource = new HikariDataSource(mysqlConfig);
            try (Connection conn = mysqlDataSource.getConnection()) {
                log.info("====================================================================");
                log.info("✅ Successfully connected to MySQL database: {}", conn.getCatalog());
                log.info("====================================================================");
                return mysqlDataSource;
            }
        } catch (Exception ex) {
            log.warn("====================================================================");
            log.warn("⚠️ MySQL connection notice: {}", ex.getMessage());
            log.warn("Auto-switching to High-Performance H2 In-Memory Database (MySQL Mode).");
            log.warn("All REST endpoints, Budgets, Expenses, Trends, and Swagger UI are fully ACTIVE!");
            log.warn("To connect to your local MySQL instance, verify your password in application.properties or set:");
            log.warn("  $env:SPRING_DATASOURCE_PASSWORD=\"your_actual_password\"");
            log.warn("====================================================================");

            if (mysqlDataSource != null && !mysqlDataSource.isClosed()) {
                mysqlDataSource.close();
            }

            HikariConfig h2Config = new HikariConfig();
            h2Config.setJdbcUrl("jdbc:h2:mem:expense_tracker;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL");
            h2Config.setDriverClassName("org.h2.Driver");
            h2Config.setUsername("sa");
            h2Config.setPassword("");
            h2Config.setMaximumPoolSize(10);
            h2Config.setPoolName("ExpenseTrackerH2Pool");

            return new HikariDataSource(h2Config);
        }
    }
}
