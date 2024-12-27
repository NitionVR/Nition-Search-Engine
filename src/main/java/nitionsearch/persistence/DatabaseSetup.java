package nitionsearch.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.UUID;

public class DatabaseSetup {
    private static final String DB_URL = "jdbc:sqlite:database.db";

    public static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS internet (" +
                "id INTEGER PRIMARY KEY, " +  // Original table has int ID
                "url TEXT NOT NULL, " +
                "body TEXT NOT NULL);";

        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void migrateIdToUUID() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement stmt = connection.createStatement()) {

            // Step 1: Create a new table with a UUID as the primary key
            String createTempTableSQL = "CREATE TABLE IF NOT EXISTS internet_new (" +
                    "id TEXT PRIMARY KEY, " +  // New table has UUID ID
                    "url TEXT NOT NULL, " +
                    "body TEXT NOT NULL);";
            stmt.execute(createTempTableSQL);

            // Step 2: Copy data from the old table to the new one with generated UUIDs
            String selectSQL = "SELECT id, url, body FROM internet;";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL);
                 ResultSet rs = selectStmt.executeQuery()) {

                String insertSQL = "INSERT INTO internet_new (id, url, body) VALUES (?, ?, ?);";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
                    while (rs.next()) {
                        String uuid = UUID.randomUUID().toString();
                        String url = rs.getString("url");
                        String body = rs.getString("body");

                        insertStmt.setString(1, uuid);  // Set UUID as new ID
                        insertStmt.setString(2, url);
                        insertStmt.setString(3, body);
                        insertStmt.executeUpdate();
                    }
                }
            }

            // Step 3: Drop the old table
            stmt.execute("DROP TABLE internet;");

            // Step 4: Rename the new table to replace the old table
            stmt.execute("ALTER TABLE internet_new RENAME TO internet;");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
        migrateIdToUUID();
    }
}
