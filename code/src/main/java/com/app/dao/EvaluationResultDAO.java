package com.app.dao;

import com.app.db.DatabaseConnection;
import com.app.entity.EvaluationResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationResultDAO {

    public int addResult(EvaluationResult result) {
        String sql = "INSERT INTO EvaluationResult(submission_id, testcase_id, status, execution_time_ms, memory_used_kb, actual_output, error_message) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, result.getSubmissionId());
            pstmt.setInt(2, result.getTestcaseId());
            pstmt.setString(3, result.getStatus());
            
            if (result.getExecutionTimeMs() != null) {
                pstmt.setInt(4, result.getExecutionTimeMs());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            
            if (result.getMemoryUsedKb() != null) {
                pstmt.setInt(5, result.getMemoryUsedKb());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            pstmt.setString(6, result.getActualOutput());
            pstmt.setString(7, result.getErrorMessage());
            
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

    public List<EvaluationResult> getAllResults() {
        List<EvaluationResult> list = new ArrayList<>();
        String sql = "SELECT * FROM EvaluationResult ORDER BY evaluated_at DESC";
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

    public List<EvaluationResult> getResultsBySubmissionId(int submissionId) {
        List<EvaluationResult> list = new ArrayList<>();
        String sql = "SELECT * FROM EvaluationResult WHERE submission_id = ? ORDER BY evaluated_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, submissionId);
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

    public void deleteResult(int id) {
        String sql = "DELETE FROM EvaluationResult WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private EvaluationResult mapRow(ResultSet rs) throws SQLException {
        EvaluationResult r = new EvaluationResult();
        r.setId(rs.getInt("id"));
        r.setSubmissionId(rs.getInt("submission_id"));
        r.setTestcaseId(rs.getInt("testcase_id"));
        r.setStatus(rs.getString("status"));
        r.setExecutionTimeMs(rs.getObject("execution_time_ms") != null ? rs.getInt("execution_time_ms") : null);
        r.setMemoryUsedKb(rs.getObject("memory_used_kb") != null ? rs.getInt("memory_used_kb") : null);
        r.setActualOutput(rs.getString("actual_output"));
        r.setErrorMessage(rs.getString("error_message"));
        r.setEvaluatedAt(rs.getTimestamp("evaluated_at"));
        return r;
    }
}
