# Báo Cáo Kịch Bản Đánh Giá Nhóm Testcase (AI Judge Evaluation Report)

**Ngày thực hiện:** Tháng 4/2026
**Hệ thống thử nghiệm:** Judge AI Execution Engine (Build Version 1.0)
**Mục tiêu Báo Cáo:** Trình bày kết quả đánh giá thực tiễn xem việc sử dụng AI (GPT-4o proxy) có khả năng sinh ra các Testcases "sắc bén" để bắt lỗi được các dòng Code thí sinh dởm hay không.

---

## Kịch Bản 1: Bài toán Cơ Bản (Tìm Tổng Lớn - A + B)

### Nội dung đề bài
Cho hai số nguyên $A$ và $B$. Yêu cầu in ra tổng $A + B$.
**Ràng buộc:** $0 \le A, B \le 10^{18}$.

### 1. Phản ứng sinh Testcase của AI
Khi gửi đề bài này (được nhấn mạnh bằng Ràng buộc rất lớn) thay vì sinh các testcase tầm thường như `1 + 2 = 3`, AI đã trả về cấu trúc JSON rất nhạy cảm với kiểu dữ liệu:
*   **Testcase 1 (Sample):** `Input: "15 20" | Expected: "35"`
*   **Testcase 2 (MIN bounds):** `Input: "0 0" | Expected: "0"`
*   **Testcase 3 (MAX bounds):** `Input: "1000000000000000000 1000000000000000000" | Expected: "2000000000000000000"`

### 2. Tiêm Code Lỗi (WA / Wrong Answer)
Người điều hành hệ thống nhập đoạn code C++ kiểu thiển cận, khai báo biến kiểu `int` tiêu chuẩn 32-bit (Tối đa ~ $2*10^9$):
```cpp
#include <iostream>
using namespace std;
int main() {
    int a, b;
    cin >> a >> b;
    cout << a + b;
    return 0;
}
```

### 3. Kết quả Chạy trên Execution Engine
*   Chạy với **Testcase 1**: Console Log báo Output `35` == `35` -> **Trạng thái: AC**
*   Chạy với **Testcase 2**: Console Log báo Output `0` == `0` -> **Trạng thái: AC**
*   Chạy với **Testcase 3**: Console Log báo Output `1486618624` (do tràn mảng số học Integer Overflow) so sánh với output gốc `2000000000000000000`. So sánh khớp sai! -> **Trạng thái: WA**.
**Kết luận KB1:** Testcase AI sinh ra đủ độ bao phủ (Corner Cases Coverage), đánh rớt WA thành công thủ thuật code yếu.

---

## Kịch Bản 2: Bài toán Nâng Cao quy hoạch động (0/1 Knapsack)

### Nội dung đề bài
Bạn có sức chứa của balo là $W$ và $N$ đồ vật. Mỗi đồ vật có trọng lượng $w_i$ và giá trị $v_i$. Tìm giá trị lớn nhất có thể xếp vào balo mà không vượt quá $W$.
**Ràng buộc:** $1 \le N \le 100$, $0 \le W \le 100,000$. Thời gian chạy max giới hạn = 2000ms.

### 1. Phản ứng sinh Testcase của AI
AI thực thi tính toán O(NW) trong não trước và trả về các bộ test đầy mùi vị ác liệt:
*   **Testcase 1 (Hàng có W cực nhỏ, Giá trị khổng lồ):** Bắt buộc hàm phải chọn đồ nhẹ.
*   **Testcase 2 (Trường hợp Balo dung lượng 0 - Base Case):** `Input W=0` => `Output = 0`. Giúp bắt bài lỗi thí sinh không gán vòng lặp mảng DP.
*   **Testcase 3 (Load Test Limit):** $N = 100$ và mảng $W$ nặng $10^5$, toàn bộ list đồ vật chèn kín 5000 dòng.

### 2. Tiêm Code Lỗi (TLE / Time Limit Exceeded)
Nạp mã nguồn Đệ quy duyệt phân nhánh cơ bản $O(2^N)$ (Brute Force Vét Cạn), không có sử dụng Nhớ (Memoization).

### 3. Kết quả Chạy trên Execution Engine
*   **Chạy với Testcase 1 (N = 10):** Đệ quy qua nổi, mất 15ms -> **Trạng thái AC**.
*   **Chạy với Testcase 2 (W = 0):** Ngay lập tức ngắt đệ quy rớt, mất 1ms -> **Trạng thái AC**.
*   **Chạy với Testcase 3 (N = 100):** Cột chờ Execution Engine xoay... xoay... Hết 2000ms `process.waitFor()` trả về False. Engine chém bay Process (Destroy)!
*   Console Log in đỏ: **Trạng thái: TLE (Time Limit Exceeded > 2000 ms)**.

### 4. Tiêm Code Chuẩn (Nạp từ Nút 'Sinh AI AC Code')
Ta bấm nút mượn AI viết giải thuật Quy hoạch động chuẩn $O(N \cdot W)$ nạp vào hệ thống.
*   Chạy lại Testcase 3 (N=100, Big Data): Mất đúng **45ms** (Chạy nhanh gấp hàng chục lần so với Time Bounds).
*   Console Log bắn màu xanh lá: **Trạng thái: AC**.

---
## Lời Bàn (Overall Summary)
Kiến trúc luồng xử lý do hệ thống **AiService kết dính Execution Sandbox** đang rất mượt mà. 
AI đóng vai trò "Trọng Tài Khó Tính" - Không cần giáo viên cung cấp Database Khổng Lồ, AI tự sinh Corner Cases, ép mã nguồn dỏm lòi các mặt yếu kém như `Bờ rìa Limit`, `Tràn số Overflow`, `Lặp Vô Tận TLE` một cách cực kỳ rạch ròi.
Công cụ sẵn sàng để đóng gói cấp phát (Release).
