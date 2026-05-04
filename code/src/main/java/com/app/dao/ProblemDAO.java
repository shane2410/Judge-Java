package com.app.dao;

import com.app.db.DatabaseConnection;
import com.app.entity.Problem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProblemDAO {

    public int addProblem(Problem problem) {
        String sql = "INSERT INTO Problem(title, description, image_path, time_limit_ms, memory_limit_kb) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, problem.getTitle());
            pstmt.setString(2, problem.getDescription());
            pstmt.setString(3, problem.getImagePath());
            pstmt.setInt(4, problem.getTimeLimitMs() > 0 ? problem.getTimeLimitMs() : 1000);
            pstmt.setInt(5, problem.getMemoryLimitKb() > 0 ? problem.getMemoryLimitKb() : 262144);
            
            pstmt.executeUpdate();
            
            // Lấy ID tự động tăng sau khi thêm
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Trả về ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Problem> getAllProblems() {
        List<Problem> list = new ArrayList<>();
        String sql = "SELECT * FROM Problem";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Problem p = new Problem();
                p.setId(rs.getInt("id"));
                p.setTitle(rs.getString("title"));
                p.setDescription(rs.getString("description"));
                p.setImagePath(rs.getString("image_path"));
                p.setTimeLimitMs(rs.getInt("time_limit_ms"));
                p.setMemoryLimitKb(rs.getInt("memory_limit_kb"));
                p.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateProblem(Problem problem) {
        String sql = "UPDATE Problem SET title=?, description=?, image_path=?, time_limit_ms=?, memory_limit_kb=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, problem.getTitle());
            pstmt.setString(2, problem.getDescription());
            pstmt.setString(3, problem.getImagePath());
            pstmt.setInt(4, problem.getTimeLimitMs());
            pstmt.setInt(5, problem.getMemoryLimitKb());
            pstmt.setInt(6, problem.getId());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteProblem(int id) {
        String sql = "DELETE FROM Problem WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
