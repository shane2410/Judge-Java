package com.app.service;

import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OcrSpaceService {
    // Key miễn phí mặc định, bạn có thể đăng ký key riêng tại ocr.space
    private static final String API_KEY = "K84606778188957"; 
    private static final String API_URL = "https://api.ocr.space/parse/image";
    private final OkHttpClient client;

    public OcrSpaceService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public String extractTextFromImage(String base64Image) throws Exception {
        // Giải mã base64 thành byte array để gửi dưới dạng file thực thụ
        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("apikey", API_KEY)
                .addFormDataPart("file", "problem.jpg", 
                    RequestBody.create(imageBytes, okhttp3.MediaType.parse("image/jpeg")))
                .addFormDataPart("language", "auto") // Quay lại vie vì gửi dạng file sẽ chuẩn hơn
                .addFormDataPart("OCREngine", "2") // Engine 2 cho tiếng Việt
                .addFormDataPart("scale", "true")
                .addFormDataPart("isOverlayRequired", "false")
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Lỗi kết nối OCR.space: " + response.code());
            }
            if (response.body() == null) {
                throw new Exception("Phản hồi từ OCR.space trống.");
            }
            String responseData = response.body().string();
            
            // Ghi log debug ra file
            writeDebugLog(responseData);

            JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
            
            if (jsonObject.has("ErrorMessage") && !jsonObject.get("ErrorMessage").isJsonNull() && !jsonObject.get("ErrorMessage").getAsString().isEmpty()) {
                throw new Exception("OCR.space Error: " + jsonObject.get("ErrorMessage").getAsString());
            }

            return jsonObject.getAsJsonArray("ParsedResults")
                    .get(0).getAsJsonObject()
                    .get("ParsedText").getAsString();
        }
    }

    private void writeDebugLog(String content) {
        try {
            java.nio.file.Files.writeString(
                java.nio.file.Paths.get("ocr_debug.log"), 
                "[" + new java.util.Date() + "] " + content + "\n\n========================================\n\n", 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND
            );
        } catch (java.io.IOException e) {
            System.err.println("Không thể ghi log debug OCR: " + e.getMessage());
        }
    }
}
