package com.app.executor;

import com.app.entity.EvaluationResult;
import com.app.entity.TestCase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CodeExecutionService {

    private static final String TEMP_DIR = "temp_workspace";

    public CodeExecutionService() {
        // Đảm bảo thư mục tạm thời để compile tồn tại
        File dir = new File(TEMP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Dịch và chạy mã nguồn đối chiếu với 1 Testcase.
     */
    public EvaluationResult evaluateSubmission(String sourceCode, String language, TestCase testCase, int timeLimitMs) {
        EvaluationResult result = new EvaluationResult();
        result.setTestcaseId(testCase.getId());

        // Tạo thư mục cách ly (sandbox thư mục) cho phiên chấm bài này
        String runId = UUID.randomUUID().toString();
        File runDir = new File(TEMP_DIR + File.separator + runId);
        runDir.mkdirs();

        try {
            boolean compiled;
            String execCommand;
            
            // 1. Lưu file và Compile
            boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
            
            if (language.equalsIgnoreCase("JAVA")) {
                File sourceFile = new File(runDir, "Main.java");
                Files.writeString(sourceFile.toPath(), sourceCode);
                compiled = compileCode("javac Main.java", runDir, result);
                
                // Trên Linux/Mac, đôi khi cần dùng đường dẫn tuyệt đối cho java
                execCommand = "java Main";
            } else if (language.equalsIgnoreCase("CPP")) {
                File sourceFile = new File(runDir, "Solution.cpp");
                Files.writeString(sourceFile.toPath(), sourceCode);
                
                File stdbitFile = new File("include/stdbit.h");
                if (stdbitFile.exists()) {
                    Files.copy(stdbitFile.toPath(), new File(runDir, "stdbit.h").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                
                String outName = isWin ? "Solution.exe" : "Solution";
                compiled = compileCode("g++ -O2 Solution.cpp -o " + outName, runDir, result);
                
                // Trên Unix cần ./ để thực thi file trong thư mục hiện tại nếu không dùng path tuyệt đối
                if (isWin) {
                    execCommand = new File(runDir, outName).getAbsolutePath();
                } else {
                    execCommand = "./" + outName;
                }
            } else {
                result.setStatus("CE");
                result.setErrorMessage("Unsupported Language");
                return result;
            }

            // Nếu gặp lỗi Compile Error
            if (!compiled) {
                return result; 
            }

            // 2. Thực thi thuật toán (Execution)
            executeCode(execCommand, runDir, testCase, timeLimitMs, result);

        } catch (IOException | InterruptedException e) {
            result.setStatus("RE");
            result.setErrorMessage("System Error: " + e.getMessage());
        } finally {
            // 3. Dọn dẹp Workspace sau khi chấm xong
            deleteDirectory(runDir);
        }

        return result;
    }

    /**
     * Hàm Compile chung sử dụng ProcessBuilder
     */
    private boolean compileCode(String command, File runDir, EvaluationResult result) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));
        pb.directory(runDir);
        Process process = pb.start();
        
        // Timeout cho việc compile (Tăng lên 30 giây cho Windows/MinGW)
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            result.setStatus("CE");
            result.setErrorMessage("Compilation Time Limit Exceeded");
            return false;
        }

        if (process.exitValue() != 0) {
            result.setStatus("CE");
            String errorMsg = readStream(process.getErrorStream());
            result.setErrorMessage(errorMsg.isEmpty() ? "Unknown Compile Error" : errorMsg);
            return false;
        }

        return true;
    }

    /**
     * Hàm Execute (Chạy) mã và đối chiếu Input/Output
     */
    private void executeCode(String command, File runDir, TestCase testCase, int timeLimitMs, EvaluationResult result) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));
        pb.directory(runDir);
        
        long startTime = System.currentTimeMillis();
        Process process = pb.start();

        // Đẩy Input vào stdin của process
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            writer.write(testCase.getInputData());
            writer.flush(); // Đẩy dữ liệu ngay lập tức
        } // Block tự động đóng OutputStream (EOF) gửi luồng kết thúc vào stdin

        // Cài đặt đồng hồ cát bắt Timer (TLE)
        boolean finished = process.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
        long endTime = System.currentTimeMillis();
        result.setExecutionTimeMs((int) (endTime - startTime));

        if (!finished) {
            process.destroyForcibly();
            result.setStatus("TLE");
            result.setErrorMessage(String.format("Time Limit Exceeded (> %d ms)", timeLimitMs));
            return;
        }

        // Bắt lỗi Runtime Error (Signal != 0)
        if (process.exitValue() != 0) {
            result.setStatus("RE");
            result.setErrorMessage(readStream(process.getErrorStream()));
            return;
        }

        // Đọc Output và So sánh
        String actualOutput = readStream(process.getInputStream());
        result.setActualOutput(actualOutput);

        // Chuẩn hóa chuỗi so sánh: Cắt bỏ khoảng trắng thừa (trim) đằng trước và đằng sau, quy đổi dấu xuống dòng.
        String normActual = actualOutput.trim().replaceAll("\\r\\n", "\n");
        String normExpected = testCase.getExpectedOutput().trim().replaceAll("\\r\\n", "\n");

        if (normActual.equals(normExpected)) {
            result.setStatus("AC");
        } else {
            result.setStatus("WA");
            // Có thể mở rộng để dùng Custom Checker nếu bài toán yêu cầu ở đây.
        }
    }

    /**
     * Helper Method: Đọc InputStream/ErrorStream thành String
     */
    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Helper Method: Xóa thư mục tạm thời
     */
    private void deleteDirectory(File currDir) {
        File[] allContents = currDir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        currDir.delete();
    }
}
