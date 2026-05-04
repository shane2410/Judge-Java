# Phân tích Thuật toán & Môi trường Chạy Code (Execution Algorithm)

Tài liệu này giải thích cách thức hoạt động của module **Execution Engine** (`CodeExecutionService.java`), một trong những module quan trọng nhất của hệ thống chấm bài. Ở đây có rủi ro bảo mật (thí sinh nộp mã kịch bản xóa ổ cứng, lặp vô hạn, hoặc cạn kiệt RAM).

## 1. Cấu trúc Quản lý "Workspace" Trực tiếp (Sandbox Thư mục)
*   **Vấn đề:** Nếu nhiều testcase hoặc nhiều bài nộp chạy song song trong cùng 1 thư mục thì có hiện tượng ghi đè file (`Main.class` hoặc `Solution.exe`).
*   **Giải pháp:** Sinh UUID (Mã định danh ngẫu nhiên tuyệt đối) để tạo thư mục con riêng biệt cho từng phiên chấm trong mục `temp_workspace`.
    *   Ví dụ: `temp_workspace/a7b1-...-33f/Main.java`.
*   **Sau khi chấm xong**, khối lệnh `finally` sẽ tiến hành dọn dẹp (Xóa đệ quy) toàn bộ thư mục UUID đó. Nó ngăn việc rác hệ thống (System trash) cày cạn ổ đĩa.

## 2. Quản lý Lỗi Hình Thành (Compile & I/O Capture)
Để lấy dữ liệu động trên luồng chuẩn (Terminal):
*   Sử dụng lệnh `process.getErrorStream()` để bắt thông báo CE (Compile Error) từ C++ Compiler (`g++`) hoặc `javac`.
*   Sử dụng `BufferedWriter` qua `process.getOutputStream()` để "Bơm" dữ liệu testcase (Input) vào `stdin` của app đang chạy. **Đặc biệt lưu ý:** Phải `.flush()` và đóng luồng (thông qua khối `try-with-resources`) báo hiệu nút EOF (End Of File) cho app thí sinh (Lỗ hổng khiến nhiều app bị treo khi chờ user nhập dữ liệu vĩnh viễn).
*   Sử dụng `process.getInputStream()` để "Vớt" dữ liệu kết quả (`stdout`) mà app in ra màn hình.

## 3. Cài Đặt Giỏ Thời Gian - Time Limit Exceeded (TLE)
Để phòng thủ các thuật toán quá tệ hoặc ngầm ý "Lặp vô hạn" (Hack System, `while(true)`):
*   Tính năng Timeout được ứng dụng cực sâu dưới thư viện OS với lệnh:
    ```java
    boolean finished = process.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
    ```
*   Hàm này khóa thread chấm tạm thời trong chớp mắt. 
    *   Nếu trả về `true`: App đã chạy xong ngon lành trước Time Limit.
    *   Nếu trả về `false`: Kẹt! App vẫn đang chạy. Ta kích hoạt **Máy chém:** `process.destroyForcibly()`. Lệnh này băm tiến trình rễ từ OS level. Kết quả ghi lại: `TLE`.

## 4. Xử lý Runtime Error (RE)
Nếu Process đang chạy sụp đổ (Ví dụ: Tràn Mảng C++, Chia cho 0 trong Java):
*   Mã máy (Exit Signal) báo cáo OS với một mã khác `0`.
*   `process.exitValue() != 0` -> Kích hoạt lệnh ghi nhận trạng thái **Runtime Error (RE)** và ném luôn bộ Error Stream về db (Làm Proof báo lỗi cho thí sinh).

## 5. Quy Chẩn Chấm Toán Học (AC vs WA Normalize)
*   Do Windows và Unix khác biệt chuẩn gõ dòng mới (`\r\n` vs `\n`), và nhiều thí sinh thích in thừa dấu cách cuối chuỗi (`   \n`).
*   Module tự động loại bỏ rủi ro xích mích hệ điều hành bằng thao tác:
    `output.trim().replaceAll("\\r\\n", "\n")`
*   Việc đối chiếu chuỗi sẽ hoàn toàn thuần khiết và công bằng tuyệt đối. Chấm trúng Output chuẩn -> `AC`, Trượt -> `WA`.

---
*Lưu ý an ninh thực tế:* Dù chúng ta cô lập được Process, nhưng chưa cô lập được tài nguyên OS. Ở Scale công nghiệp, thay vì `ProcessBuilder`, nên thay thế tầng `execCommand` bằng việc đẩy file vào **Docker Container**, với flag `--memory=256m` và `--cpus="1.0"`. (Tuy nhiên Desktop Application JavaFX theo Phase 1 dùng `ProcessBuilder` là phương án tốt nhất).
