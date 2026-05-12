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

public class DeepSeekService {

    // private static final String API_KEY = System.getenv("DEEPSEEK_API_KEY");
    private static final String API_KEY = "sk-ca9212c89a0b4a7dba68060790972ddb";
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private final OkHttpClient client;
    private final Gson gson;

    public DeepSeekService() {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("CẢNH BÁO LỚN: Chưa cấu hình biến môi trường DEEPSEEK_API_KEY!");
        }
        this.gson = new Gson();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS) 
                .build();
    }

    private String callLLM(String systemPrompt, String userMessage) throws Exception {
        return callLLM(systemPrompt, userMessage, -1);
    }

    private String callLLM(String systemPrompt, String userMessage, int maxTokens) throws Exception {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new Exception("LỖI: Chưa thiết lập DEEPSEEK_API_KEY trong máy.");
        }

        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.addProperty("model", "deepseek-v4-flash");

        JsonArray messages = new JsonArray();

        // System message
        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", systemPrompt);
        messages.add(sysMsg);

        // User message
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);

        requestBodyJson.add("messages", messages);
        requestBodyJson.addProperty("temperature", 0.2);
        if (maxTokens > 0) {
            requestBodyJson.addProperty("max_tokens", maxTokens);
        }
        requestBodyJson.addProperty("stream", false);

        RequestBody body = RequestBody.create(
                requestBodyJson.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("LỖI HTTP " + response.code() + ": " + response.message());
            }
            String responseData = response.body().string();
            JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
            
            return jsonObject.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        } catch (java.net.UnknownHostException e) {
             throw new Exception("LỖI MẠNG: Không thể kết nối tới DeepSeek API.");
        }
    }

    public String extractTextFromImage(String base64Image) throws Exception {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new Exception("LỖI: Chưa thiết lập DEEPSEEK_API_KEY trong máy.");
        }

        // Sử dụng chính xác cấu trúc Payload JSON theo yêu cầu của user
        String jsonPayload = """
            {
              "model": "deepseek-v4-flash",
              "messages": [
                {
                  "role": "user",
                  "content": [
                    { "type": "text", "text": "Hãy trích xuất văn bản từ hình ảnh này, giữ nguyên định dạng toán học và code." },
                    { "type": "image_url", "image_url": { "url": "data:image/jpeg;base64,%s" } }
                  ]
                }
              ],
              "stream": false
            }
            """.formatted(base64Image);

        RequestBody body = RequestBody.create(
                jsonPayload,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                throw new Exception("LỖI HTTP " + response.code() + ": " + errorBody);
            }
            String responseData = response.body().string();
            JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
            
            return jsonObject.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        }
    }

    /**
     * Tinh chỉnh văn bản OCR bị lỗi bằng AI
     */
    public String refineOcrText(String noisyText) throws Exception {
        String systemPrompt = "Bạn là một chuyên gia sửa lỗi văn bản OCR. " +
                "Dưới đây là nội dung một đề bài lập trình (ICPC/IOI) vừa được quét bằng công cụ OCR cũ nên có thể bị lỗi font, sai ký tự (ví dụ: 'took' thay vì 'rook', 'Sanpìs' thay vì 'Sample').\n" +
                "Nhiệm vụ của bạn:\n" +
                "1. Sửa lại các từ bị sai chính tả/font cho đúng ngữ nghĩa tiếng Anh/Việt.\n" +
                "2. Giữ nguyên các thông số toán học, công thức và cấu trúc đề bài.\n" +
                "3. Trả về văn bản đã được định dạng Markdown đẹp mắt.\n" +
                "KHÔNG giải thích gì thêm, chỉ trả về nội dung đã sửa.";
        
        return callLLM(systemPrompt, "NỘI DUNG OCR LỖI:\n" + noisyText);
    }

    public String generateTestCases(String problemContent, int numberOfTestCases) throws Exception {
        String systemPrompt = "You are an ICPC/IOI testcase generator. Output ONLY a raw JSON array of " + numberOfTestCases + " objects. " +
                "Each object must have exactly 2 keys: \"input\" and \"expected_output\". " +
                "Include corner cases (min/max bounds, zero, empty, edge cases). " +
                "Reply with ONLY the JSON array starting with [ and ending with ]. No markdown, no explanations.";
        
        String response = callLLM(systemPrompt, "ĐỀ BÀI:\n" + problemContent);
        // Clean response
        response = response.trim();
        if (response.startsWith("```")) {
            int start = response.indexOf("\n") + 1;
            int end = response.lastIndexOf("```");
            response = (end > start) ? response.substring(start, end).trim() : response.substring(start).trim();
        }
        // Fix truncated JSON: remove last incomplete entry
        if (response.endsWith(",")) {
            int lastBracket = response.lastIndexOf("}");
            if (lastBracket > 0) response = response.substring(0, lastBracket + 1);
        } else if (!response.endsWith("]")) {
            int lastComplete = response.lastIndexOf("}");
            if (lastComplete > 0) response = response.substring(0, lastComplete + 1) + "\n]";
        }
        return response.trim();
    }

    public String generateChecker(String problemContent, String language) throws Exception {
        String systemPrompt = "Bạn là C++ / Java Expert. Đề bài này có thể có nhiều đáp án hợp lệ. " +
                "Hãy viết một chương trình CUSTOM CHECKER bằng ngôn ngữ " + language + ".\n" +
                "Checker sẽ nhận đầu vào từ file input, file user_output và file answer theo arg. " +
                "In ra stdout 'AC' nếu đúng, in ra 'WA' nếu sai. " +
                "KHÔNG giải thích, CHỈ TRẢ VỀ MÃ NGUỒN được bọc trong ```" + language.toLowerCase() + " và ```";
        
        String response = callLLM(systemPrompt, "ĐỀ BÀI:\n" + problemContent);
        return extractCodeBlock(response, language.toLowerCase());
    }

    public String generateSampleCode(String problemContent, String language) throws Exception {
        String systemPrompt = "You are a Grandmaster competitive programmer. Write an optimal " + language + " solution. " +
                "Output ONLY the code wrapped in ```" + language.toLowerCase() + "```. No explanations.";

        String response = callLLM(systemPrompt, "ĐỀ BÀI:\n" + problemContent);
        return extractCodeBlock(response, language.toLowerCase());
    }

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
