package de.leycm.orm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class OrmSqlConfig {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String driverName;

    private HikariDataSource dataSource;

    public OrmSqlConfig(String host, int port, String database, String username, String password, String driverName) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.driverName = driverName;
        init();
    }

    private void init() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:" + driverName + "://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverName);
        config.setMaximumPoolSize(10); // optional
        config.setMinimumIdle(2);
        config.setPoolName("OrmPool");
        config.setAutoCommit(true);

        this.dataSource = new HikariDataSource(config);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
