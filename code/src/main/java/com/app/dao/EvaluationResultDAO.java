package com.app.dao;

import com.app.db.DatabaseConnection;
import com.app.entity.EvaluationResult;

import java.sql.*;

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
}
