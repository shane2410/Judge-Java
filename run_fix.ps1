# Thiết lập Encoding để xử lý tiếng Việt
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "--- Dang khoi dong JudgeAI (Phien ban sua loi duong dan) ---" -ForegroundColor Cyan

# 1. Thiet lap Java
$javaPath = "C:\Users\hihihihi\.antigravity\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64"
$env:JAVA_HOME = $javaPath
$env:Path = "$javaPath\bin;" + $env:Path

# 2. Di chuyen vao thu muc code bang duong dan tuong doi
Set-Location -Path "$PSScriptRoot\code"

# 3. Thu chay bang Maven qua Bash
Write-Host "> Dang thu chay bang Maven..."
bash ./mvnw javafx:run

# 4. Neu Maven loi, thu chay truc tiep bang Java voi Classpath tuong doi
if ($LASTEXITCODE -ne 0) {
    Write-Host "> Maven gap loi (co the do thieu JDK), dang thu chay truc tiep bang Java JRE..." -ForegroundColor Yellow
    
    $m2 = "C:\Users\hihihihi\.m2\repository"
    $cp = "target/classes;" + 
          "$m2\org\openjfx\javafx-controls\17.0.2\javafx-controls-17.0.2.jar;" +
          "$m2\org\openjfx\javafx-controls\17.0.2\javafx-controls-17.0.2-win.jar;" +
          "$m2\org\openjfx\javafx-graphics\17.0.2\javafx-graphics-17.0.2.jar;" +
          "$m2\org\openjfx\javafx-graphics\17.0.2\javafx-graphics-17.0.2-win.jar;" +
          "$m2\org\openjfx\javafx-base\17.0.2\javafx-base-17.0.2.jar;" +
          "$m2\org\openjfx\javafx-base\17.0.2\javafx-base-17.0.2-win.jar;" +
          "$m2\org\openjfx\javafx-fxml\17.0.2\javafx-fxml-17.0.2.jar;" +
          "$m2\org\openjfx\javafx-fxml\17.0.2\javafx-fxml-17.0.2-win.jar;" +
          "$m2\org\xerial\sqlite-jdbc\3.41.2.2\sqlite-jdbc-3.41.2.2.jar;" +
          "$m2\com\squareup\okhttp3\okhttp\4.10.0\okhttp-4.10.0.jar;" +
          "$m2\com\squareup\okio\okio-jvm\3.0.0\okio-jvm-3.0.0.jar;" +
          "$m2\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar;" +
          "$m2\org\jetbrains\kotlin\kotlin-stdlib\1.6.20\kotlin-stdlib-1.6.20.jar;" +
          "$m2\org\jetbrains\kotlin\kotlin-stdlib-common\1.5.31\kotlin-stdlib-common-1.5.31.jar;" +
          "$m2\org\jetbrains\annotations\13.0\annotations-13.0.jar"

    java -cp $cp com.app.Launcher
}

Write-Host "--- Hoan thanh ---"
pause
