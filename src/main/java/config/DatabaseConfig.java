package config;

public class DatabaseConfig {
    public static final String DB_URL = "jdbc:mysql://localhost:3306/pharmsdb";
    public static final String USER = "root";
    public static final String PASS = "Mayur@1234";
    
    private DatabaseConfig() {
        // Private constructor to prevent instantiation
    }
} 