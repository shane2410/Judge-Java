#!/bin/bash

# Script khởi chạy JudgeAI cho Linux/Mac
echo "--- Đang khởi động JudgeAI (Linux/Mac Version) ---"

# 1. Kiểm tra Java
if ! command -v java &> /dev/null
then
    echo "Lỗi: Không tìm thấy Java. Vui lòng cài đặt JDK 17 hoặc 21."
    exit 1
fi

# 2. Di chuyển vào thư mục code
cd "$(dirname "$0")/code" || exit

# 3. Thử chạy bằng Maven Wrapper
echo "> Đang thử chạy bằng Maven..."
chmod +x mvnw
./mvnw javafx:run

# 4. Nếu Maven lỗi, thử chạy trực tiếp bằng Java (Yêu cầu đã compile trước đó)
if [ $? -ne 0 ]; then
    echo "> Maven gặp lỗi, đang thử chạy trực tiếp bằng Java..."
    
    # Thiết lập Classpath (Giả định các thư viện đã được tải về thư mục .m2 mặc định)
    M2_REPO="$HOME/.m2/repository"
    CP="target/classes:\
$M2_REPO/org/openjfx/javafx-controls/17.0.2/javafx-controls-17.0.2.jar:\
$M2_REPO/org/openjfx/javafx-controls/17.0.2/javafx-controls-17.0.2-linux.jar:\
$M2_REPO/org/openjfx/javafx-controls/17.0.2/javafx-controls-17.0.2-mac.jar:\
$M2_REPO/org/openjfx/javafx-graphics/17.0.2/javafx-graphics-17.0.2.jar:\
$M2_REPO/org/openjfx/javafx-graphics/17.0.2/javafx-graphics-17.0.2-linux.jar:\
$M2_REPO/org/openjfx/javafx-graphics/17.0.2/javafx-graphics-17.0.2-mac.jar:\
$M2_REPO/org/openjfx/javafx-base/17.0.2/javafx-base-17.0.2.jar:\
$M2_REPO/org/openjfx/javafx-base/17.0.2/javafx-base-17.0.2-linux.jar:\
$M2_REPO/org/openjfx/javafx-base/17.0.2/javafx-base-17.0.2-mac.jar:\
$M2_REPO/org/openjfx/javafx-fxml/17.0.2/javafx-fxml-17.0.2.jar:\
$M2_REPO/org/openjfx/javafx-fxml/17.0.2/javafx-fxml-17.0.2-linux.jar:\
$M2_REPO/org/openjfx/javafx-fxml/17.0.2/javafx-fxml-17.0.2-mac.jar:\
$M2_REPO/org/xerial/sqlite-jdbc/3.41.2.2/sqlite-jdbc-3.41.2.2.jar:\
$M2_REPO/com/squareup/okhttp3/okhttp/4.10.0/okhttp-4.10.0.jar:\
$M2_REPO/com/squareup/okio/okio-jvm/3.0.0/okio-jvm-3.0.0.jar:\
$M2_REPO/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar:\
$M2_REPO/org/jetbrains/kotlin/kotlin-stdlib/1.6.20/kotlin-stdlib-1.6.20.jar:\
$M2_REPO/org/jetbrains/kotlin/kotlin-stdlib-common/1.5.31/kotlin-stdlib-common-1.5.31.jar:\
$M2_REPO/org/jetbrains/annotations/13.0/annotations-13.0.jar"

    java -cp "$CP" com.app.Launcher
fi
