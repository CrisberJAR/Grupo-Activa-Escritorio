package com.crediactiva.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.sql.DataSource;

public class ConnectionFactory {
    private static HikariDataSource ds;

    private static synchronized void init() {
        if (ds != null) return;
        try (InputStream in = ConnectionFactory.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new RuntimeException("No se encontró el archivo db.properties en resources");
            }
            Properties props = new Properties();
            props.load(in);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String pass = props.getProperty("db.password");
            int poolSize = Integer.parseInt(props.getProperty("db.pool.size", "5"));

            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(url);
            cfg.setUsername(user);
            cfg.setPassword(pass);
            cfg.setMaximumPoolSize(poolSize);
            cfg.setMinimumIdle(Math.min(2, poolSize));
            cfg.setPoolName("CrediActivaPool");
            cfg.addDataSourceProperty("cachePrepStmts", "true");
            cfg.addDataSourceProperty("prepStmtCacheSize", "250");
            cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            ds = new HikariDataSource(cfg);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando db.properties", e);
        }
    }

    public static DataSource getDataSource() {
        if (ds == null) init();
        return ds;
    }
}
