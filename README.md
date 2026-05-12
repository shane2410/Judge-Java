# JudgeAI - Hệ Thống Chấm Bài Tự Động Tích Hợp Trí Tuệ Nhân Tạo

JudgeAI là một hệ thống chấm bài lập trình thi đấu (Competitive Programming) được xây dựng bằng JavaFX, SQLite và tích hợp AI (DeepSeek) để tự động hóa toàn bộ quy trình: từ việc nhận diện ảnh đề bài bằng OCR, sinh Testcase, tạo Checker, đến việc biên dịch và chấm điểm mã nguồn của thí sinh.

## 🌟 Tính Năng Nổi Bật
- **OCR Hybrid:** Hỗ trợ nhận diện đề bài từ ảnh bằng VietOCR3 (Offline) kết hợp với AI DeepSeek để tinh chỉnh, chuẩn hóa văn bản.
- **Tự động sinh Testcase & Giải thuật:** Ứng dụng AI để sinh Testcase đa dạng (bao gồm cả corner cases) và sinh mã giải thuật tối ưu (Reference Code).
- **Chấm bài tự động (Evaluation):** Hỗ trợ biên dịch và chạy sandbox cho C++17 và Java. Đánh giá trạng thái (AC, WA, TLE, CE, RE) và đo lường thời gian/bộ nhớ.
- **Quản lý lịch sử:** Lưu trữ lịch sử chấm bài, cho phép nạp lại (Load to Judge) các bài tập cũ để tiếp tục chấm.
- **Đa nền tảng:** Hoạt động trơn tru trên Windows, macOS và Linux.

---

## 💻 Yêu Cầu Hệ Thống (Prerequisites)
Để chạy được ứng dụng, máy tính của bạn cần cài đặt sẵn:
1. **Java Development Kit (JDK):** Phiên bản **17** trở lên. (Đảm bảo biến môi trường `JAVA_HOME` đã được cấu hình).
2. **C++ Compiler (MinGW/GCC):** Yêu cầu có `g++` trong System PATH để hệ thống có thể biên dịch code C++ của thí sinh. (Với Windows, nên cài MinGW-w64).
3. **Maven (Tùy chọn):** Khuyến nghị cài đặt Maven để dễ dàng tải thư viện và build project, hoặc có thể sử dụng `mvnw` đi kèm.
4. **Internet Connection:** Yêu cầu bắt buộc để gọi API của DeepSeek AI.

---

## ⚙️ Hướng Dẫn Cài Đặt

### Bước 1: Cấu hình biến môi trường API Key (Quan trọng)
Vì lý do bảo mật, API Key của DeepSeek không được gán cứng vào mã nguồn. Bạn bắt buộc phải cấu hình biến môi trường `DEEPSEEK_API_KEY` trước khi chạy ứng dụng.

- **Trên Windows:**
  1. Mở Start Menu, gõ "Environment Variables" -> Chọn "Edit the system environment variables".
  2. Chọn nút "Environment Variables...".
  3. Ở mục "System variables" (hoặc User variables), bấm "New...".
  4. Variable name: `DEEPSEEK_API_KEY`
  5. Variable value: `[Điền_API_Key_của_bạn_vào_đây]`
  6. Khởi động lại IDE hoặc Terminal để nhận biến môi trường mới.

- **Trên macOS/Linux:**
  Mở terminal và thêm dòng sau vào file `~/.bashrc` hoặc `~/.zshrc`:
  ```bash
  export DEEPSEEK_API_KEY="[Điền_API_Key_của_bạn_vào_đây]"
  ```
  Sau đó chạy lệnh `source ~/.bashrc`.

### Bước 2: Tải các thư viện phụ thuộc (Dependencies)
Mở Terminal / Command Prompt tại thư mục `code/`, chạy lệnh sau để Maven tự động tải các thư viện (JavaFX, SQLite, OkHttp, Gson...):
```bash
./mvnw clean install
```
*(Nếu bạn dùng Windows PowerShell, có thể gõ `bash ./mvnw clean install`)*

---

## 🚀 Hướng Dẫn Chạy Ứng Dụng

Hệ thống cung cấp sẵn các kịch bản chạy (scripts) tiện lợi, giúp vượt rào cản Module của JavaFX 11+:

**Cách 1: Sử dụng Script (Dành cho người dùng cuối)**
- **Trên Windows:** Click đúp vào file `run_fix.ps1` (hoặc chuột phải chọn "Run with PowerShell").
- **Trên macOS / Linux:** Mở Terminal và chạy:
  ```bash
  chmod +x run_app.sh
  ./run_app.sh
  ```

**Cách 2: Chạy trực tiếp qua IDE (VS Code / IntelliJ / Eclipse)**
- Mở project thư mục `code/` bằng IDE của bạn.
- Mở file `src/main/java/com/app/Launcher.java`.
- Bấm nút **Run** (biểu tượng ▶️) ở cạnh hàm `main`. 
  *(Lưu ý: Bạn phải chạy file `Launcher.java` thay vì `MainApp.java` để tránh lỗi "JavaFX runtime components are missing").*

**Cách 3: Chạy bằng Maven Command**
Tại thư mục `code/`:
```bash
mvn javafx:run
```

---

## 📖 Hướng Dẫn Sử Dụng

### 1. Tab "Judge" (Khu vực chấm bài chính)
Tại màn hình chính, quy trình sử dụng diễn ra theo chu trình 4 bước:

- **Bước 1: Quét Đề Bài (OCR)**
  Bấm nút **"📷 Upload Ảnh (OCR)"**, chọn một ảnh chứa nội dung đề bài. Hệ thống sẽ quét chữ qua VietOCR (offline) và sau đó tự động gửi sang DeepSeek AI để tinh chỉnh lại font lỗi, trả về văn bản Markdown sạch sẽ tại ô "Nội dung đề bài".
- **Bước 2: Phân Tích & Sinh Testcase**
  Chọn số lượng Testcase mong muốn ở hộp thoại "Số lượng testcase", sau đó bấm **"🧠 Phân tích & Sinh Testcase"**. AI sẽ dựa vào đề bài để tạo ra các testcase bao phủ mọi trường hợp (Corner cases). Bạn có thể xem bảng kết quả ở giữa màn hình.
- **Bước 3: Nhập Mã Nguồn (Code)**
  Viết code của bạn vào khung "Mã nguồn (Submission)" ở bên phải, chọn ngôn ngữ tương ứng (JAVA hoặc CPP). Hoặc bạn có thể bấm **"✨ AI tạo giải thuật AC"** để AI tự động code giúp một bản chuẩn (Reference Solution).
- **Bước 4: Chấm Bài**
  Bấm nút **"▶️ Bắt Đầu Chấm"**. Hệ thống sẽ:
  - Khởi tạo thư mục Sandbox (`temp_workspace`).
  - Copy mã nguồn và thư viện (VD: `stdbit.h`) vào Sandbox.
  - Tiến hành biên dịch (Javac/G++).
  - Chạy code với lần lượt các testcase và so sánh kết quả. 
  - Thông báo điểm số cuối cùng (AC, WA, CE, TLE...). 
  *Lưu ý: Chỉ những bài nộp đúng toàn bộ (Tất cả Testcase đều đạt AC) mới được tự động lưu vào Lịch Sử.*

### 2. Tab "History" (Lịch Sử)
- Chuyển sang Tab "History" để xem lại các phiên chấm bài thành công trước đó (dữ liệu được load từ file `judgeai.db`).
- Bấm vào một bài tập bất kỳ và nhấn nút **"🔄 Nạp lại vào Judge"** để hệ thống tự động bê toàn bộ Đề bài, Testcase và Mã nguồn của phiên đó trở lại giao diện chấm bài chính, giúp bạn dễ dàng xem lại hoặc tối ưu thêm mã nguồn.

---

## 🛠 Cấu Trúc Thư Mục Quan Trọng
- `/code`: Chứa mã nguồn dự án Java (Maven).
- `/VietOCR3`: Công cụ nhận dạng ký tự quang học offline.
- `/code/include`: Thư mục chứa thư viện C++ mở rộng (VD: `stdbit.h`).
- `/code/judgeai.db`: Cơ sở dữ liệu SQLite cục bộ (Sẽ tự tạo nếu chưa có).
- `/code/temp_workspace`: Khu vực Sandbox để chạy code (Tự động dọn dẹp sau khi chấm).

Chúc bạn có những trải nghiệm tuyệt vời với JudgeAI! Cần hỗ trợ thêm, vui lòng liên hệ tác giả.
