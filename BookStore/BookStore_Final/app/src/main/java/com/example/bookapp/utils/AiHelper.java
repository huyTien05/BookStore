//package com.example.bookapp.utils;
//
//import android.os.Handler;
//import android.os.Looper;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.util.Scanner;
//
///**
// * Helper đơn giản để gọi Google Gemini API.
// * Dùng HttpURLConnection thuần - không cần thêm thư viện.
// */
//public class AiHelper {
//
//    // ⚠️ DÁN API KEY GEMINI CỦA BẠN VÀO ĐÂY
//    // Lấy miễn phí tại: https://aistudio.google.com/
//    private static final String API_KEY = "AIzaSyCWgNL9MammvrP-_8nf1B7vJa_SgZO5gnU";
//
//    // Sử dụng model gemini-2.5-flash (tốc độ cao, tối ưu cho ứng dụng di động)
//    private static final String MODEL = "gemini-2.5-flash";
//    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent?key=" + API_KEY;
//
//    public interface Callback {
//        void onResult(String result);      // Gọi trên Main thread khi thành công
//        void onError(String errorMsg);     // Gọi trên Main thread khi lỗi
//    }
//
//    /**
//     * Gửi prompt đến Gemini AI, trả kết quả qua callback (không block UI).
//     */
//    public static void ask(String prompt, Callback callback) {
//        Handler mainHandler = new Handler(Looper.getMainLooper());
//
//        new Thread(() -> {
//            try {
//                // 1. Build JSON body theo đúng cấu trúc của Gemini
//                // Cấu trúc mong muốn: { "contents": [{ "parts": [{ "text": "prompt_cua_ban" }] }] }
//                JSONObject textObject = new JSONObject();
//                textObject.put("text", prompt);
//
//                JSONArray partsArray = new JSONArray();
//                partsArray.put(textObject);
//
//                JSONObject partsObject = new JSONObject();
//                partsObject.put("parts", partsArray);
//
//                JSONArray contentsArray = new JSONArray();
//                contentsArray.put(partsObject);
//
//                JSONObject body = new JSONObject();
//                body.put("contents", contentsArray);
//
//                // 2. Gửi HTTP request
//                URL url = new URL(API_URL);
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Content-Type", "application/json");
//                conn.setDoOutput(true);
//                conn.setConnectTimeout(15000);
//                conn.setReadTimeout(30000);
//
//                byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
//                try (OutputStream os = conn.getOutputStream()) {
//                    os.write(input);
//                }
//
//                // 3. Đọc response từ Server
//                int code = conn.getResponseCode();
//                java.io.InputStream stream = (code >= 200 && code < 300)
//                        ? conn.getInputStream() : conn.getErrorStream();
//
//                Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name());
//                String responseStr = scanner.useDelimiter("\\A").next();
//                scanner.close();
//
//                if (code != 200) {
//                    mainHandler.post(() -> callback.onError("Lỗi từ server: " + code));
//                    return;
//                }
//
//                // 4. Parse JSON kết quả trả về của Gemini
//                // Cấu trúc nhận được: candidates[0].content.parts[0].text
//                JSONObject json = new JSONObject(responseStr);
//                String reply = json.getJSONArray("candidates")
//                        .getJSONObject(0)
//                        .getJSONObject("content")
//                        .getJSONArray("parts")
//                        .getJSONObject(0)
//                        .getString("text");
//
//                // Trả kết quả về giao diện chính
//                mainHandler.post(() -> callback.onResult(reply));
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                mainHandler.post(() -> callback.onError("Không kết nối được Gemini API"));
//            }
//        }).start();
//    }
//}
package com.example.bookapp.utils;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Helper đơn giản để gọi Google Gemini API.
 * Dùng HttpURLConnection thuần - không cần thêm thư viện.
 *
 * ⚠️ HƯỚNG DẪN CÀI API KEY:
 *   1. Vào https://aistudio.google.com/ → "Get API key" → tạo key mới
 *   2. Thay chuỗi "DAN_API_KEY_CUA_BAN_VAO_DAY" bên dưới bằng key của bạn
 *   3. KHÔNG push API key lên GitHub (thêm vào .gitignore hoặc dùng local.properties)
 */
public class AiHelper {

    // ⚠️ THAY API KEY CỦA BẠN VÀO ĐÂY
    private static final String API_KEY = "AIzaSyCKxz8MjeztZDOEYzXiymtLsfwYhfLBXUg";

    // gemini-1.5-flash: miễn phí, ổn định - KHÔNG dùng gemini-2.5-flash (mất phí)
    private static final String MODEL = "gemini-2.5-flash";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent?key=" + API_KEY;

    public interface Callback {
        void onResult(String result);   // Gọi trên Main thread khi thành công
        void onError(String errorMsg);  // Gọi trên Main thread khi lỗi
    }

    /**
     * Gửi prompt đến Gemini AI, trả kết quả qua callback (không block UI).
     */
    public static void ask(String prompt, Callback callback) {
        // Kiểm tra API key trước khi gọi
        if (API_KEY == null || API_KEY.trim().isEmpty() || API_KEY.equals("DAN_API_KEY_CUA_BAN_VAO_DAY")) {
            new Handler(Looper.getMainLooper()).post(() ->
                    callback.onError("Chưa cấu hình API Key. Vui lòng thêm Gemini API Key vào AiHelper.java")
            );
            return;
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                // 1. Build JSON body theo cấu trúc Gemini API
                JSONObject textPart = new JSONObject();
                textPart.put("text", prompt);

                JSONArray parts = new JSONArray();
                parts.put(textPart);

                JSONObject content = new JSONObject();
                content.put("parts", parts);

                JSONArray contents = new JSONArray();
                contents.put(content);

                JSONObject body = new JSONObject();
                body.put("contents", contents);

                // 2. Mở kết nối HTTP
                URL url = new URL(API_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(input);
                }

                // 3. Đọc response
                int code = conn.getResponseCode();
                java.io.InputStream stream = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name());
                String responseStr = scanner.useDelimiter("\\A").next();
                scanner.close();

                if (code != 200) {
                    // Cố gắng đọc thông báo lỗi từ server
                    String errMsg = "Lỗi server: HTTP " + code;
                    try {
                        JSONObject errJson = new JSONObject(responseStr);
                        if (errJson.has("error")) {
                            errMsg += " - " + errJson.getJSONObject("error").optString("message", "");
                        }
                    } catch (Exception ignored) {}
                    final String finalErr = errMsg;
                    mainHandler.post(() -> callback.onError(finalErr));
                    return;
                }

                // 4. Parse JSON kết quả Gemini
                // Cấu trúc: candidates[0].content.parts[0].text
                JSONObject json = new JSONObject(responseStr);

                // Kiểm tra promptFeedback nếu bị block
                if (json.has("promptFeedback")) {
                    JSONObject feedback = json.getJSONObject("promptFeedback");
                    if (feedback.has("blockReason")) {
                        mainHandler.post(() -> callback.onError("Nội dung bị chặn bởi AI: " + feedback.optString("blockReason")));
                        return;
                    }
                }

                JSONArray candidates = json.optJSONArray("candidates");
                if (candidates == null || candidates.length() == 0) {
                    mainHandler.post(() -> callback.onError("AI không trả về kết quả. Thử lại sau."));
                    return;
                }

                String reply = candidates
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                mainHandler.post(() -> callback.onResult(reply));

            } catch (java.net.SocketTimeoutException e) {
                mainHandler.post(() -> callback.onError("Kết nối bị timeout. Kiểm tra internet và thử lại."));
            } catch (java.net.UnknownHostException e) {
                mainHandler.post(() -> callback.onError("Không có internet. Kiểm tra kết nối mạng."));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError("Lỗi kết nối AI: " + e.getMessage()));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
