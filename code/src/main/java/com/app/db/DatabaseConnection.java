package com.app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Tên file SQLite Database, sẽ được tạo tại đường dẫn gốc của app.
    private static final String DB_URL = "jdbc:sqlite:judgeai.db";
    private static Connection connection = null;

    // Lấy kết nối dùng chung (Singleton pattern đơn giản)
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Yêu cầu thư viện tải driver (đảm bảo cho các bản JDBC cũ)
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                
                // Khởi tạo bảng ngay khi kết nối nếu chúng chưa tồn tại
                initializeDatabase(connection);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Database Connection Error: " + e.getMessage());
        }
        return connection;
    }

    // Tự động khởi tạo schema.sql (Rất hữu ích khi chép sang máy khác)
    private static void initializeDatabase(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Problem (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "image_path VARCHAR(500), " +
                    "time_limit_ms INTEGER DEFAULT 1000, " +
                    "memory_limit_kb INTEGER DEFAULT 262144, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS TestCase (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "problem_id INTEGER NOT NULL, " +
                    "input_data TEXT NOT NULL, " +
                    "expected_output TEXT NOT NULL, " +
                    "is_hidden BOOLEAN DEFAULT 1, " +
                    "strength_score INTEGER DEFAULT 0, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(problem_id) REFERENCES Problem(id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS CodeSubmission (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "problem_id INTEGER NOT NULL, " +
                    "source_code TEXT NOT NULL, " +
                    "language VARCHAR(50) NOT NULL, " +
                    "expected_verdict VARCHAR(20) DEFAULT 'UNKNOWN', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(problem_id) REFERENCES Problem(id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS EvaluationResult (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "submission_id INTEGER NOT NULL, " +
                    "testcase_id INTEGER NOT NULL, " +
                    "status VARCHAR(20) NOT NULL, " +
                    "execution_time_ms INTEGER, " +
                    "memory_used_kb INTEGER, " +
                    "actual_output TEXT, " +
                    "error_message TEXT, " +
                    "evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(submission_id) REFERENCES CodeSubmission(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(testcase_id) REFERENCES TestCase(id) ON DELETE CASCADE)");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_evaluation_submission ON EvaluationResult(submission_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_evaluation_testcase ON EvaluationResult(testcase_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_testcase_problem ON TestCase(problem_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_codesubmission_problem ON CodeSubmission(problem_id)");
                    
            // Enable Foreign keys for SQLite 
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
    }
}
