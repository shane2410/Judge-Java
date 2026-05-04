# Thiết kế Cơ sở Dữ liệu (Database Schema)

Hệ thống sẽ sử dụng cơ sở dữ liệu quan hệ (SQLite ưu tiên cho Desktop App hoặc MySQL) để lưu trữ Đề bài, Testcase chuẩn, Mã nguồn nộp lên và Kết quả chạy.

Dưới đây là thiết kế chi tiết các bảng (Tables).

## Tổng quan Sơ đồ (ERD Diagram Text)
*   **Problem** `1` --- `N` **TestCase**
*   **Problem** `1` --- `N` **CodeSubmission**
*   **CodeSubmission** `1` --- `N` **EvaluationResult**
*   **TestCase** `1` --- `N` **EvaluationResult**

---

## Chi tiết các bảng (Tables)

### 1. Bảng `Problem` (Đề bài thi)
Lưu trữ thông tin gốc của bài toán, các yêu cầu và giới hạn (Constraints).

| Tên Cột | Kiểu Dữ Liệu | Khóa | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PK, Auto | ID tự tăng định danh bài toán |
| `title` | VARCHAR(255) | | Tiêu đề bài toán |
| `description` | TEXT | | Nội dung văn bản của đề đã bóc tách từ ảnh/nhập tay |
| `image_path` | VARCHAR(500) | | (Tùy chọn) Đường dẫn ảnh gốc nạp vào |
| `time_limit_ms` | INTEGER | | Giới hạn thời gian mặc định (vd: 1000ms = 1 giây) |
| `memory_limit_kb`| INTEGER | | Giới hạn bộ nhớ (vd: 262144 = 256MB) |
| `created_at` | TIMESTAMP | | Thời gian tạo |

### 2. Bảng `TestCase` (Bộ test sinh bởi AI)
Lưu danh sách Input/Output do AI sinh ra dựa trên đề bài.

| Tên Cột | Kiểu Dữ Liệu | Khóa | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PK, Auto | ID tự tăng |
| `problem_id` | INTEGER | FK | Ràng buộc tới bảng `Problem` |
| `input_data` | TEXT | | Nội dung Input (stdin) |
| `expected_output`| TEXT | | Nội dung Output chuẩn mong muốn (stdout) |
| `is_hidden` | BOOLEAN | | True nếu là test ẩn (cố tình giấu lúc thi), False nếu test mẫu (Sample) |
| `strength_score` | INTEGER | | (1-5) Độ mạnh của testcase đánh giá bởi AI hoặc thực nghiệm |
| `created_at` | TIMESTAMP | | Thời gian tạo |

### 3. Bảng `CodeSubmission` (Mã nguồn Mẫu / Mã nộp)
Lưu trữ mã nguồn được cấu hình sẵn (AC, WA, TLE) để chấm thử độ khó của testcase, hoặc code AI tự động sinh.

| Tên Cột | Kiểu Dữ Liệu | Khóa | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PK, Auto | ID tự tăng |
| `problem_id` | INTEGER | FK | Ràng buộc tới bảng `Problem` |
| `source_code` | TEXT | | File mã nguồn thô chứa code thuật toán |
| `language` | VARCHAR(50) | | Ngôn ngữ nộp bài (vd: `JAVA`, `CPP`, `PYTHON`) |
| `expected_verdict`| VARCHAR(20) | | Loại code: `AC` (Đúng chuẩn), `WA` (Sai thuật), `TLE` (Chậm), `UNKNOWN` |
| `created_at` | TIMESTAMP | | Thời gian tạo |

### 4. Bảng `EvaluationResult` (Kết quả chạy Test)
Lưu kết quả chi tiết của từng `CodeSubmission` với từng `TestCase`. Bảng này rất quan trọng để lập Báo Cáo Chất lượng (Report).

| Tên Cột | Kiểu Dữ Liệu | Khóa | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PK, Auto | ID tự tăng |
| `submission_id` | INTEGER | FK | Ràng buộc tới `CodeSubmission` |
| `testcase_id` | INTEGER | FK | Ràng buộc tới `TestCase` |
| `status` | VARCHAR(20) | | Kết quả chạy thực tế: `AC`, `WA`, `TLE`, `MLE`, `RE`, `CE` |
| `execution_time_ms`| INTEGER | | Thời gian chạy thực tế đo được (ms) |
| `memory_used_kb` | INTEGER | | Bộ nhớ RAM thực tế tiêu thụ (nếu đo được) |
| `actual_output`| TEXT | | Output thực tế mà mã nguồn đã in ra (dùng truy vết WA) |
| `error_message`| TEXT | | Lưu thông báo lỗi nếu Compile Error hoặc Runtime Error |
| `evaluated_at` | TIMESTAMP | | Thời điểm chấm |
