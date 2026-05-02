import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final HikariDataSource pool;

    static {
        HikariConfig cfg = new HikariConfig();
        // Supabase Transaction Pooler — reuse connections instead of opening a new one per call
        cfg.setJdbcUrl("jdbc:postgresql://aws-1-ap-southeast-2.pooler.supabase.com:5432/postgres"
                + "?sslmode=require&prepareThreshold=0");
        cfg.setUsername("postgres.vcsbzxouqdaispptawmt");
        cfg.setPassword("5604Asadbek");
        cfg.setMaximumPoolSize(5);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(30_000);
        cfg.setIdleTimeout(300_000);
        cfg.setMaxLifetime(600_000);
        pool = new HikariDataSource(cfg);
    }

    public static Connection connect() throws SQLException {
        return pool.getConnection();
    }
}