package nitionsearch.persistence;

import nitionsearch.model.Page;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class PageDAOImpl implements PageDAO {
    private static final String DB_URL = "jdbc:sqlite:database.db";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    @Override
    public Optional<Page> findByUrl(String url) {
        String query = "SELECT * FROM internet WHERE url = ?";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, url);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToPage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Page save(Page page) {
        String insertSQL = "INSERT INTO internet (id, url, body) VALUES (?, ?, ?)";
        String updateSQL = "UPDATE internet SET url = ?, body = ? WHERE id = ?";

        try (Connection connection = getConnection()) {
            if (findById(page.getId()).isPresent()) {
                try (PreparedStatement stmt = connection.prepareStatement(updateSQL)) {
                    stmt.setString(1, page.getUrl());
                    stmt.setString(2, page.getContent());
                    stmt.setString(3, page.getId().toString());
                    stmt.executeUpdate();
                }
            } else {
                try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
                    stmt.setString(1, page.getId().toString());
                    stmt.setString(2, page.getUrl());
                    stmt.setString(3, page.getContent());
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return page;
    }

    @Override
    public Optional<Page> findById(UUID id) {
        String query = "SELECT * FROM internet WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToPage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Collection<Page> searchByKeyword(String keyword) {
        String query = "SELECT * FROM internet WHERE body LIKE ?";
        List<Page> result = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(mapRowToPage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    private Page mapRowToPage(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String url = rs.getString("url");
        String content = rs.getString("body");
        return new Page(id, url, content);
    }
}
