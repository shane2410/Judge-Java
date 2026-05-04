# Hướng dẫn Cài đặt & Sử dụng Hệ thống JudgeAI

Tài liệu này cung cấp hướng dẫn cài đặt từ đầu và cách thức ứng dụng JudgeAI (được xây dựng trên JavaFX + SQLite + AI Toolings) vận hành để đánh giá chất lượng testcase.

## 1. Yêu cầu Hệ thống (Prerequisites)

Để hệ thống có thể dịch code C++ và Java, chạy AI API, máy tính cần được cài đặt các phần mềm sau:

- **Java Development Kit (JDK):** Yêu cầu phiên bản JDK 17 (hoặc mới hơn). Đảm bảo biến môi trường `JAVA_HOME` đã được thiết lập.
- **Apache Maven:** Sử dụng để tự động kéo các thư viện OkHttp, Gson và JDBC.
- **C++ Compiler:**
  - _Trên Windows:_ Yêu cầu cài đặt **MinGW-w64** (chứa `g++`) và thêm vào đường dẫn `PATH` của hệ thống.
  - _Trên Linux/Mac:_ Cài đặt `g++` (thường có sẵn hoặc qua `sudo apt install g++`).
- **LLM API Key:** Bạn cần một API Key của OpenAI (`sk-...`) hoặc bất kỳ nền tảng GPT-4o tương tự hỗ trợ RESTful API JSON.

## 2. Hướng dẫn Cài đặt (Setup)

**Bước 1: Cấu hình mã nguồn & API Key**

1. Mở Project với IntelliJ IDEA VÀO hoặc Visual Studio Code.
2. Điều hướng tới file `/code/src/main/java/com/app/service/AiService.java`.
3. Thay thế dòng `private static final String API_KEY = "YOUR_API_KEY_HERE";` bằng API Key thật của bạn.

**Bước 2: Cập nhật thư viện Maven**

- Chạy lệnh `mvn clean install` tại thư mục `/code` để tải các file .jar (JavaFX, SQLite, v.v.).

**Bước 3: Khởi chạy Ứng dụng**

- Do những hạn chế của Java Platform Module System trên JDK lớn hơn 11, thay vì chạy trực tiếp `MainApp.java`, hãy chạy file `com.app.Launcher.java`.
- _Lệnh Terminal tương đương:_ `mvn javafx:run` (Nếu cấu hình plugin) hoặc Run trên IDE cho file `Launcher`. CSDL `judgeai.db` sẽ tự động được tạo tại thư mục gốc trong lần khởi chạy đầu tiên.

## 3. Quy trình Sử dụng (Usage Workflow)

Hệ thống được thiết kế thành một luồng chạy từ Trái qua Phải trên giao diện người dùng.

**Giai đoạn 1: Khởi tạo Đề Bài (Nhập Liệu)**

1. Mở ứng dụng, khu vực bên trái biểu thị không gian để nhập đề bài thi.
2. Dán nội dung text (bao gồm cả Ràng buộc bộ nhớ/thời gian nếu có) vào ô văn bản lớn.
3. _Tính năng mở rộng:_ Nếu có ảnh màn hình, nhấn "Upload Ảnh (OCR)" để Vision LLM tự động trích xuất text (Lưu ý: API gốc ở cấu hình bản mẫu hiện chưa có payload cho base64 ảnh, bạn cần bổ sung đường dẫn theo cấu trúc đã note).

**Giai đoạn 2: Trích xuất Testcase thông minh**

1. Nhấn nút xanh lá cây **"Phân tích & Sinh Testcase"**.
2. Hãy đợi vài giây, API sẽ giao tiếp với mô hình AI (`gpt-4o`). Nó sẽ bẻ gãy đề bài ra thành các Corner Cases, biên dịch lại thành JSON.
3. Giao diện (Center) sẽ tự động thêm và render kết quả lên Table View. Code nền tự động Insert DB.

**Giai đoạn 3: Phép Thử Sinh Code chuẩn**

1. Ở cột Phải, trước khi chạy thử, nếu bạn không có Code đáp án chuẩn mẫu thì hãy đổi sang NGÔN NGỮ mong muốn (C++ hoặc Java).
2. Nhấn nút **"AI Tự Sinh Code (AC)"**. Khung soạn thảo dưới sẽ được đổ đầy thuật toán giải chuẩn O(1) hoặc O(N) tùy bài.

**Giai đoạn 4: Execution Engine & Dashboard (Kiểm định Chất lượng)**

1. Bạn có thể cố tình sửa code giải thuật lại thành Code bị sai (Thiếu dấu, sai logic điều kiện) để kiểm thử Testcase có Đủ Mạnh và Ép Lỗi được hay không.
2. Nhấn nút xanh dương **"Chạy Thử Nghiệm"**.
3. Tiến trình diễn ra ngầm tại thư mục `temp_workspace/`. Các thông số về Output, `Compile Error`, `Runtime Error` hoặc nhịp `Time Limit (TLE)` sẽ hiển thị ngay lập tức lên Console Log bên dưới.
4. Đối sánh Output chuẩn của AI Testcase vs Output thuật toán vừa chạy để in ra kết quả cuối: **AC** (Đúng toàn bộ) hoặc **WA** (Sai một phần/Toàn phần).
