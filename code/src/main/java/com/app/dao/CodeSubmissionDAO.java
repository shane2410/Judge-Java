package com.app.dao;

import com.app.db.DatabaseConnection;
import com.app.entity.CodeSubmission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CodeSubmissionDAO {

    public int addSubmission(CodeSubmission submission) {
        String sql = "INSERT INTO CodeSubmission(problem_id, source_code, language, expected_verdict) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, submission.getProblemId());
            pstmt.setString(2, submission.getSourceCode());
            pstmt.setString(3, submission.getLanguage());
            pstmt.setString(4, submission.getExpectedVerdict() != null ? submission.getExpectedVerdict() : "UNKNOWN");
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<CodeSubmission> getAllSubmissions() {
        List<CodeSubmission> list = new ArrayList<>();
        String sql = "SELECT * FROM CodeSubmission ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<CodeSubmission> getSubmissionsByProblemId(int problemId) {
        List<CodeSubmission> list = new ArrayList<>();
        String sql = "SELECT * FROM CodeSubmission WHERE problem_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, problemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deleteSubmission(int id) {
        String sql = "DELETE FROM CodeSubmission WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private CodeSubmission mapRow(ResultSet rs) throws SQLException {
        CodeSubmission s = new CodeSubmission();
        s.setId(rs.getInt("id"));
        s.setProblemId(rs.getInt("problem_id"));
        s.setSourceCode(rs.getString("source_code"));
        s.setLanguage(rs.getString("language"));
        s.setExpectedVerdict(rs.getString("expected_verdict"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }
}
