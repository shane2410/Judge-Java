package com.app.service;

import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiService {
    
    // private static final String API_KEY = "AIzaSyBZcpMJ-Pn08vP5Z0iLyBUE-5OxhoEeVZY";
    private static final String API_KEY = System.getenv("GEMINI_API_KEY") != null ? System.getenv("GEMINI_API_KEY") : "AIzaSyBZcpMJ-Pn08vP5Z0iLyBUE-5OxhoEeVZY"; // Fallback cho mục đích dev

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    private final OkHttpClient client;
    private final Gson gson;

    public AiService() {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("CẢNH BÁO LỚN: Chưa cấu hình biến môi trường GEMINI_API_KEY!");
        }
        this.gson = new Gson();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS) 
                .build();
    }

    private String callLLM(String systemPrompt, String userMessage) throws Exception {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new Exception("LỖI: Chưa thiết lập GEMINI_API_KEY trong máy.");
        }

        // 1. system_instruction (cho Gemini)
        JsonObject systemInstruction = new JsonObject();
        JsonObject systemParts = new JsonObject();
        systemParts.addProperty("text", systemPrompt);
        systemInstruction.add("parts", systemParts);

        // 2. contents (lời nhắc người dùng)
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray userPartsArray = new JsonArray();
        JsonObject userText = new JsonObject();
        userText.addProperty("text", userMessage);
        userPartsArray.add(userText);
        userContent.add("parts", userPartsArray);

        JsonArray contents = new JsonArray();
        contents.add(userContent);

        // 3. generationConfig
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.2);

        // Đóng gói request body cho Gemini API
        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.add("system_instruction", systemInstruction);
        requestBodyJson.add("contents", contents);
        requestBodyJson.add("generationConfig", generationConfig);

        RequestBody body = RequestBody.create(
                requestBodyJson.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        String urlWithKey = API_URL + "?key=" + API_KEY;

        Request request = new Request.Builder()
                .url(urlWithKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // XỬ LÝ LỖI TRẢ VỀ ĐỂ HIỂN THỊ LÊN GUI
                if (response.code() == 429) {
                    throw new Exception("LỖI 429: Tài khoản API của bạn đã hết tiền (Out of quota) hoặc bị giới hạn tốc độ.");
                } else if (response.code() == 400) {
                    throw new Exception("LỖI 400: Request không hợp lệ (API Key sai hoặc payload lỗi).");
                } else {
                    throw new Exception("LỖI HTTP " + response.code() + ": " + response.message());
                }
            }
            String responseData = response.body().string();
            JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
            
            // Trích xuất response text từ cấu trúc của Gemini
            return jsonObject.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (java.net.UnknownHostException e) {
             throw new Exception("LỖI MẠNG: Không thể kết nối tới Google Gemini API (Kiểm tra mạng hoặc DNS/VPN).");
        }
    }

    /**
     * 1. Xử lý ảnh đề bài thành text (OCR / Vision)
     */
    public String extractTextFromImage(String base64Image) throws Exception {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new Exception("LỖI: Chưa thiết lập GEMINI_API_KEY trong máy.");
        }

        // 1. system_instruction (cho Gemini)
        JsonObject systemInstruction = new JsonObject();
        JsonObject systemParts = new JsonObject();
        systemParts.addProperty("text", "Bạn là một công cụ OCR. Nhiệm vụ của bạn là trích xuất toàn bộ văn bản có trong ảnh một cách chính xác nhất. Đừng giải thích gì thêm, chỉ trả về văn bản trích xuất được.");
        systemInstruction.add("parts", systemParts);

        // 2. contents (ảnh)
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray userPartsArray = new JsonArray();
        JsonObject inlineDataObj = new JsonObject();
        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mime_type", "image/png"); // Mặc định PNG, có thể dùng cho JPEG
        inlineData.addProperty("data", base64Image);
        inlineDataObj.add("inline_data", inlineData);
        userPartsArray.add(inlineDataObj);
        userContent.add("parts", userPartsArray);

        JsonArray contents = new JsonArray();
        contents.add(userContent);

        // 3. generationConfig
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.0);

        // Đóng gói request body cho Gemini API
        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.add("system_instruction", systemInstruction);
        requestBodyJson.add("contents", contents);
        requestBodyJson.add("generationConfig", generationConfig);

        RequestBody body = RequestBody.create(
                requestBodyJson.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        String urlWithKey = API_URL + "?key=" + API_KEY;

        Request request = new Request.Builder()
                .url(urlWithKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("LỖI HTTP " + response.code() + ": " + response.message());
            }
            String responseData = response.body().string();
            JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
            
            return jsonObject.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (java.net.UnknownHostException e) {
             throw new Exception("LỖI MẠNG: Không thể kết nối tới Google Gemini API (Kiểm tra mạng hoặc DNS/VPN).");
        }
    }

    /**
     * 2. Sinh Testcase: Trả về chuỗi JSON mảng
     * Chuỗi trả về có định dạng: [{"input": "...", "expected_output": "...", "explanation": "..."}]
     */
    public String generateTestCases(String problemContent, int numberOfTestCases) throws Exception {
        String systemPrompt = "Bạn là một Chuyên gia Sinh Testcase Đề thi Lập trình (ICPC/IOI). " +
                "Nhiệm vụ của bạn là đọc đề bài, hiểu rõ Input/Output constraints, và sinh ra một danh sách " + numberOfTestCases + " testcases vững chắc.\n" +
                "YÊU CẦU BẮT BUỘC:\n" +
                "- Phải bao gồm các corner cases (MAX, MIN, giá trị 0, mảng rỗng nếu có thể).\n" +
                "- KHÔNG giải thích lằng nhằng ngoài chuỗi JSON.\n" +
                "- Định dạng trả về BẮT BUỘC là JSON Array of Objects: [ {\"input\": \"...\", \"expected_output\": \"...\", \"explanation\": \"...\"} ]\n" +
                "Bao bọc JSON bằng ```json và ```";
        
        String response = callLLM(systemPrompt, "ĐỀ BÀI:\n" + problemContent);
        // Làm sạch response để lấy array JSON thuần
        if (response.contains("```json")) {
            response = response.substring(response.indexOf("```json") + 7);
            if (response.contains("```")) {
                response = response.substring(0, response.lastIndexOf("```"));
            }
        }
        return response.trim();
    }

    /**
     * 3. Sinh Custom Checker (dành cho bài toán có nhiều cách ra kết quả)
     */
    public String generateChecker(String problemContent, String language) throws Exception {
        String systemPrompt = "Bạn là C++ / Java Expert. Đề bài này có thể có nhiều đáp án hợp lệ. " +
                "Hãy viết một chương trình CUSTOM CHECKER bằng ngôn ngữ " + language + ".\n" +
                "Checker sẽ nhận đầu vào từ file input, file user_output và file answer theo arg. " +
                "In ra stdout 'AC' nếu đúng, in ra 'WA' nếu sai. " +
                "KHÔNG giải thích, CHỈ TRẢ VỀ MÃ NGUỒN được bọc trong ```" + language.toLowerCase() + " và ```";
        
        String response = callLLM(systemPrompt, "ĐỀ BÀI:\n" + problemContent);
        return extractCodeBlock(response, language.toLowerCase());
    }

    /**
     * 4. Sinh Sample Code (AC - Accepted Code)
     */
    public String generateSampleCode(String problemContent, String language) throws Exception {
        String systemPrompt = "Bạn là thí sinh thi ICPC mức độ Grandmaster. " +
                "Hãy viết mã nguồn thuật toán để giải quyết đề bài sau. " +
                "Ngôn ngữ: " + language + ". Yêu cầu tối ưu Complexity để qua được Time Limit. " +
                "KHÔNG giải thích, CHỈ TRẢ VỀ MÃ NGUỒN được bọc trong ```" + language.toLowerCase() + " và ```";

        String response = callLLM(systemPrompt, "ĐỀ BÀI:\n" + problemContent);
        return extractCodeBlock(response, language.toLowerCase());
    }

    /**
     * Utilities: Lấy phần code ra khỏi block markdown
     */
    private String extractCodeBlock(String markdown, String lang) {
        String prefix = "```" + lang;
        if (markdown.contains(prefix)) {
            String code = markdown.substring(markdown.indexOf(prefix) + prefix.length());
            if (code.contains("```")) {
                code = code.substring(0, code.indexOf("```"));
            }
            return code.trim();
        }
        // Fallback
        return markdown;
    }
}
