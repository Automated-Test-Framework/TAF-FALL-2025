package ca.etsmtl.taf.exportimport.repositories;

import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.*;

@Component
public class TestRailMappingRepository {

    private static final String DB_PATH =
            System.getenv().getOrDefault("DB_PATH", "data/testrail_cache.db");
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    public static final String PROJECT_KEY_SUFFIX = "PROJECT:";
    public static final String TEST_SUITE_KEY_SUFFIX = "SUITE:";
    public static final String SECTION_KEY_SUFFIX = "SECTION:";
    public static final String TEST_CASE_KEY_SUFFIX = "CASE:";
    public static final String TEST_RUN_KEY_SUFFIX = "RUN:";
    public static final String TEST_RESULT_KEY_SUFFIX = "RESULT:";

    public TestRailMappingRepository() {
        File dbDir = new File("/app/data");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String createTable = """
                CREATE TABLE IF NOT EXISTS cache (
                    key TEXT PRIMARY KEY,
                    id INT NOT NULL
                );
            """;
            conn.createStatement().execute(createTable);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize SQLite store", e);
        }
    }

    public void put(String key, Integer id) {
        String sql = "INSERT OR REPLACE INTO cache (key, id) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to put id in store", e);
        }
    }

    public Integer get(String key) {
        String sql = "SELECT id FROM cache WHERE key = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("id") : null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get id from store", e);
        }
    }
}
