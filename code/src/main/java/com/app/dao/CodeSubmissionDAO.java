package com.app.dao;

import com.app.db.DatabaseConnection;
import com.app.entity.CodeSubmission;

import java.sql.*;

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
}
