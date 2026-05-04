# Thiết kế Kiến trúc Hệ thống

## 1. Mô hình Kiến trúc: MVC (Model-View-Controller) / Clean Architecture

Dự án được xây dựng dựa trên nguyên tắc phân lớp rõ ràng để dễ dàng mở rộng và bảo trì, đặc biệt khi tích hợp JavaFX và các API bên ngoài.

### Cấu trúc Thư mục (Package Structure)

```
/code
 ├── src/main/java/com/judgeai/
 │    ├── JudgeAIApplication.java        # Entry point của ứng dụng
 │    ├── controllers/                   # JavaFX Controllers (View logic)
 │    │    ├── MainController.java
 │    │    ├── ProblemInputController.java
 │    │    └── EvaluationController.java
 │    ├── models/                        # Domain Models/Entities
 │    │    ├── Problem.java
 │    │    ├── TestCase.java
 │    │    ├── CodeSubmission.java
 │    │    └── EvaluationResult.java
 │    ├── services/                      # Business Logic (Use Cases)
 │    │    ├── ai/                       # Tích hợp Gemini API
 │    │    │    ├── AIVisionService.java # Trích xuất OCR
 │    │    │    ├── AITestGenerator.java # Sinh testcase và checker
 │    │    │    └── AICodeGenerator.java # Sinh code AC
 │    │    ├── execution/                # Môi trường chạy code C++/Java (ProcessBuilder)
 │    │    │    ├── CompilerService.java
 │    │    │    └── ExecutionEngine.java
 │    │    └── database/                 # Quản lý Database (Repository pattern)
 │    │         └── DatabaseManager.java # SQLite / MySQL connection
 │    └── utils/                         # Các helper classes
 │         ├── ConfigReader.java
 │         └── FileHelper.java
 ├── src/main/resources/                 # Chứa FXML, CSS, Images, SQL properties
 │    ├── fxml/                          # Các file giao diện JavaFX
 │    ├── css/                           # File styles (Vanilla CSS cho JavaFX)
 │    └── application.properties         # Cấu hình API key, database
 ├── pom.xml / build.gradle              # Cấu hình dependencies (Maven/Gradle)
 └── schema.sql                          # File khởi tạo DB gốc
```

## 2. Luồng hoạt động (Workflow Cốt Lõi)

**Giai đoạn 1: Nhập và Phân tích Đề Bài**

1. Người dùng nhập văn bản đề bài hoặc upload ảnh đề bài thông qua Giao diện (GUI).
2. Nếu là ảnh, hệ thống gọi `AIVisionService` (OCR qua OpenAI Vision hoặc Gemini) để chuyển thành Text.
3. Đề bài (Text) được lưu vào Database (`Problem`).

**Giai đoạn 2: AI Phân tích & Sinh Testcase**

1. Hệ thống gửi đề bài tới `AITestGenerator` qua API (Gemini). Prompt yêu cầu: Phân tích edge cases, giới hạn (constraints), và sinh ra danh sách `TestCase` (Input/Output).
2. Nếu đề có nhiều đáp án (multiple valid outputs), AI sẽ sinh thêm `Custom Checker` (thường bằng C++ hoặc Python).
3. Testcases được lưu vào Database.

**Giai đoạn 3: Thực thi và Đánh giá Testcase (Evaluation Engine)**

1. Người dùng nhập mã nguồn mẫu (AC - Accepted, WA - Wrong, TLE - Time Limit). Nếu không có, `AICodeGenerator` tự động sinh mã giải chuẩn.
2. Mã nguồn được lưu vào `CodeSubmission`.
3. `CompilerService` biên dịch mã nguồn.
4. `ExecutionEngine` chạy file thực thi với từng `TestCase` (đưa Input vào `stdin`, đọc Output từ `stdout`).
5. Kết quả (Passed, Failed do sai output, TLE, CE) được lưu vào `EvaluationResult`.
6. Hệ thống đánh giá độ phân loại của Testcase (testcase AC có bắt được lỗi thẻ WA, TLE không).

**Giai đoạn 4: Báo cáo**

1. Controller tổng hợp dữ liệu từ `EvaluationResult` và hiển thị trên bảng thống kê JavaFX.

## 3. Giải pháp Công nghệ cho Execution Engine

Trong Java, chúng ta sẽ sử dụng `java.lang.ProcessBuilder` để xử lý việc compile và run mã nguồn từ bên ngoài một cách đồng bộ/bất đồng bộ.

### Điểm nhấn bảo mật và tính ổn định:

- **Cách ly tiến trình (Process Sandboxing cơ bản):** Mỗi file code được biên dịch ra một thư mục tạm `temp/exec/`, và chỉ chạy trong đó.
- **Quản lý bộ nhớ (Memory Limit):** Sử dụng các cờ của JVM hoặc command args như `ulimit` (nếu chạy trên Unix) để giới hạn RAM. (Sẽ mô phỏng thông qua API Java tiêu chuẩn nếu trên Windows).
- **Time Limit Exceeded (TLE):** Sử dụng hàm `Process.waitFor(timeout, TimeUnit)` cực kỳ quan trọng để bắt lỗi chạy quá thời gian (lặp vô hạn).
  - _Ví dụ:_ `boolean finished = process.waitFor(2, TimeUnit.SECONDS); if(!finished) { process.destroyForcibly(); return TLE; }`
- **Bắt lỗi biên dịch (Compile Error - CE):** Lấy dữ liệu từ luồng lỗi `process.getErrorStream()` của lệnh `javac` hoặc `g++`.
- **Xóa dọn (Clean up):** Sử dụng `FileHelper` để đảm bảo `.class`, `.exe` và file tạm được xóa sau khi đánh giá xong để tránh tràn bộ nhớ ổ đĩa tĩnh.
