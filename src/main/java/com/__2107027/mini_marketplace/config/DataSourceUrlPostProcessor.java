package com.__2107027.mini_marketplace.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * Normalizes the datasource URL so that plain postgresql:// URIs (e.g. the
 * DATABASE_URL injected by Render) are converted to the jdbc:postgresql://
 * scheme required by the JDBC driver before the datasource bean is created.
 */
public class DataSourceUrlPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_KEY = "spring.datasource.url";
    private static final String PLAIN_PREFIX = "postgresql://";
    private static final String JDBC_PREFIX = "jdbc:postgresql://";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        String url = environment.getProperty(PROPERTY_KEY);
        if (url != null && url.startsWith(PLAIN_PREFIX)) {
            String jdbcUrl = JDBC_PREFIX + url.substring(PLAIN_PREFIX.length());
            environment.getPropertySources().addFirst(
                    new MapPropertySource("datasourceUrlNormalizer", Map.of(PROPERTY_KEY, jdbcUrl)));
        }
    }
}
