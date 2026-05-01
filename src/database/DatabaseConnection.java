import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = setting("LIBRARY_DB_URL", "jdbc:postgresql:postgresql://neondb_owner:npg_VZHeupG4RgQ6@ep-late-pine-a46et8x9-pooler.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require");
    private static final String USER = setting("LIBRARY_DB_USER", "postgres");
    private static final String PASSWORD = setting("LIBRARY_DB_PASSWORD", "0884");

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String setting(String name, String defaultValue) {
        String systemProperty = System.getProperty(name);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String environmentVariable = System.getenv(name);
        if (environmentVariable != null && !environmentVariable.isBlank()) {
            return environmentVariable;
        }

        return defaultValue;
    }
}
