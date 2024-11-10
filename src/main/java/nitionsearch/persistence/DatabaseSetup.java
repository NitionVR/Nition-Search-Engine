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
                "id TEXT PRIMARY KEY, " +
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

            stmt.execute("ALTER TABLE internet ADD COLUMN temp_uuid TEXT;");

            String selectSQL = "SELECT rowid FROM internet;";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL);
                 ResultSet rs = selectStmt.executeQuery()) {

                String updateSQL = "UPDATE internet SET temp_uuid = ? WHERE rowid = ?;";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSQL)) {
                    while (rs.next()) {
                        String uuid = UUID.randomUUID().toString();
                        int rowId = rs.getInt("rowid");
                        updateStmt.setString(1, uuid);
                        updateStmt.setInt(2, rowId);
                        updateStmt.executeUpdate();
                    }
                }
            }

            stmt.execute("ALTER TABLE internet DROP COLUMN id;");
            stmt.execute("ALTER TABLE internet RENAME COLUMN temp_uuid TO id;");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
        migrateIdToUUID();
    }
}
