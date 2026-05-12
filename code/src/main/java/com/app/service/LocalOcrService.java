package com.app.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class LocalOcrService {
    private String getVietOcrDir() {
        File dir1 = new File("VietOCR3");
        if (dir1.exists() && dir1.isDirectory()) return dir1.getAbsolutePath();
        File dir2 = new File("../VietOCR3");
        if (dir2.exists() && dir2.isDirectory()) return dir2.getAbsolutePath();
        return dir1.getAbsolutePath();
    }

    public String extractTextFromFile(File imageFile) throws Exception {
        if (!imageFile.exists()) {
            throw new Exception("Không tìm thấy file ảnh đầu vào: " + imageFile.getAbsolutePath());
        }

        // Tên file tạm bên trong thư mục VietOCR3 để tránh lỗi đường dẫn chứa dấu cách hoặc ký tự lạ
        String tempInputName = "ocr_input_" + System.currentTimeMillis() + ".jpg";
        String tempOutputBase = "ocr_output_" + System.currentTimeMillis();
        
        String vietOcrDirPath = getVietOcrDir();
        File vietOcrDir = new File(vietOcrDirPath);
        File tempInputFile = new File(vietOcrDir, tempInputName);
        File expectedOutputFile = new File(vietOcrDir, tempOutputBase + ".txt");

        try {
            // 1. Copy ảnh vào thư mục VietOCR3 để đảm bảo VietOCR tìm thấy
            Files.copy(imageFile.toPath(), tempInputFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // 2. Chạy lệnh CLI
            // Lưu ý: Dùng "java" thay vì đường dẫn tuyệt đối để linh hoạt
            ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", "VietOCR.jar", 
                tempInputName, 
                tempOutputBase, 
                "-l", "vie"
            );
            pb.directory(vietOcrDir);
            pb.environment().put("TESSDATA_PREFIX", ".");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Đọc log output
            StringBuilder logBuffer = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logBuffer.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new Exception("VietOCR CLI lỗi (Exit " + exitCode + "): \n" + logBuffer.toString());
            }

            // 3. Đọc kết quả
            if (!expectedOutputFile.exists()) {
                throw new Exception("VietOCR chạy xong nhưng không tạo ra file kết quả. \nLog: " + logBuffer.toString());
            }
            
            String result = Files.readString(expectedOutputFile.toPath(), java.nio.charset.StandardCharsets.UTF_8).trim();
            return result;

        } finally {
            // 4. Dọn dẹp
            if (tempInputFile.exists()) tempInputFile.delete();
            if (expectedOutputFile.exists()) expectedOutputFile.delete();
        }
    }

}
