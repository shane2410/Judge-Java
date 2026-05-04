-- Tùy chỉnh cho SQLite (hoạt động tốt với MySQL nếu chỉnh cấu trúc Auto_Increment thành AUTO_INCREMENT)
-- Khởi tạo DB Schema cho Judge AI System

-- 1. Bảng lưu trữ Thông tin Đề Bài
CREATE TABLE IF NOT EXISTS Problem (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_path VARCHAR(500),
    time_limit_ms INTEGER DEFAULT 1000,
    memory_limit_kb INTEGER DEFAULT 262144,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng lưu trữ Testcases sinh ra từ AI
CREATE TABLE IF NOT EXISTS TestCase (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    problem_id INTEGER NOT NULL,
    input_data TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    is_hidden BOOLEAN DEFAULT 1,
    strength_score INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(problem_id) REFERENCES Problem(id) ON DELETE CASCADE
);

-- 3. Bảng lưu trữ Mã nguồn Mẫu (AC, WA, TLE...) hoặc AI generated code
CREATE TABLE IF NOT EXISTS CodeSubmission (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    problem_id INTEGER NOT NULL,
    source_code TEXT NOT NULL,
    language VARCHAR(50) NOT NULL, -- 'JAVA', 'CPP'
    expected_verdict VARCHAR(20) DEFAULT 'UNKNOWN', -- 'AC', 'WA', 'TLE'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(problem_id) REFERENCES Problem(id) ON DELETE CASCADE
);

-- 4. Bảng lưu kết quả chạy của từng CodeSubmission đối với từng TestCase
CREATE TABLE IF NOT EXISTS EvaluationResult (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    submission_id INTEGER NOT NULL,
    testcase_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL, -- 'AC', 'WA', 'TLE', 'RE', 'CE', 'MLE'
    execution_time_ms INTEGER,
    memory_used_kb INTEGER,
    actual_output TEXT,
    error_message TEXT,
    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(submission_id) REFERENCES CodeSubmission(id) ON DELETE CASCADE,
    FOREIGN KEY(testcase_id) REFERENCES TestCase(id) ON DELETE CASCADE
);

-- Tạo Index hỗ trợ truy vấn nhanh báo cáo kết quả
CREATE INDEX idx_evaluation_submission ON EvaluationResult (submission_id);
CREATE INDEX idx_evaluation_testcase ON EvaluationResult (testcase_id);
CREATE INDEX idx_testcase_problem ON TestCase (problem_id);
CREATE INDEX idx_codesubmission_problem ON CodeSubmission (problem_id);
