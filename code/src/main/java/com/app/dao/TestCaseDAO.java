package com.app.dao;

import com.app.db.DatabaseConnection;
import com.app.entity.TestCase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestCaseDAO {

    public int addTestCase(TestCase tc) {
        String sql = "INSERT INTO TestCase(problem_id, input_data, expected_output, is_hidden, strength_score) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, tc.getProblemId());
            pstmt.setString(2, tc.getInputData());
            pstmt.setString(3, tc.getExpectedOutput());
            pstmt.setBoolean(4, tc.isHidden());
            pstmt.setInt(5, tc.getStrengthScore());
            
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

    public List<TestCase> getTestCasesByProblemId(int problemId) {
        List<TestCase> list = new ArrayList<>();
        String sql = "SELECT * FROM TestCase WHERE problem_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, problemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TestCase tc = new TestCase();
                    tc.setId(rs.getInt("id"));
                    tc.setProblemId(rs.getInt("problem_id"));
                    tc.setInputData(rs.getString("input_data"));
                    tc.setExpectedOutput(rs.getString("expected_output"));
                    tc.setHidden(rs.getBoolean("is_hidden"));
                    tc.setStrengthScore(rs.getInt("strength_score"));
                    tc.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(tc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateTestCase(TestCase tc) {
        String sql = "UPDATE TestCase SET input_data=?, expected_output=?, is_hidden=?, strength_score=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, tc.getInputData());
            pstmt.setString(2, tc.getExpectedOutput());
            pstmt.setBoolean(3, tc.isHidden());
            pstmt.setInt(4, tc.getStrengthScore());
            pstmt.setInt(5, tc.getId());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTestCase(int id) {
        String sql = "DELETE FROM TestCase WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
