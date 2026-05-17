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
 */
public class AiHelper {

    // ⚠️ DÁN API KEY GEMINI CỦA BẠN VÀO ĐÂY
    // Lấy miễn phí tại: https://aistudio.google.com/
    private static final String API_KEY = "AIzaSyCWgNL9MammvrP-_8nf1B7vJa_SgZO5gnU";

    // Sử dụng model gemini-2.5-flash (tốc độ cao, tối ưu cho ứng dụng di động)
    private static final String MODEL = "gemini-2.5-flash";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent?key=" + API_KEY;

    public interface Callback {
        void onResult(String result);      // Gọi trên Main thread khi thành công
        void onError(String errorMsg);     // Gọi trên Main thread khi lỗi
    }

    /**
     * Gửi prompt đến Gemini AI, trả kết quả qua callback (không block UI).
     */
    public static void ask(String prompt, Callback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            try {
                // 1. Build JSON body theo đúng cấu trúc của Gemini
                // Cấu trúc mong muốn: { "contents": [{ "parts": [{ "text": "prompt_cua_ban" }] }] }
                JSONObject textObject = new JSONObject();
                textObject.put("text", prompt);

                JSONArray partsArray = new JSONArray();
                partsArray.put(textObject);

                JSONObject partsObject = new JSONObject();
                partsObject.put("parts", partsArray);

                JSONArray contentsArray = new JSONArray();
                contentsArray.put(partsObject);

                JSONObject body = new JSONObject();
                body.put("contents", contentsArray);

                // 2. Gửi HTTP request
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(input);
                }

                // 3. Đọc response từ Server
                int code = conn.getResponseCode();
                java.io.InputStream stream = (code >= 200 && code < 300)
                        ? conn.getInputStream() : conn.getErrorStream();

                Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name());
                String responseStr = scanner.useDelimiter("\\A").next();
                scanner.close();

                if (code != 200) {
                    mainHandler.post(() -> callback.onError("Lỗi từ server: " + code));
                    return;
                }

                // 4. Parse JSON kết quả trả về của Gemini
                // Cấu trúc nhận được: candidates[0].content.parts[0].text
                JSONObject json = new JSONObject(responseStr);
                String reply = json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                // Trả kết quả về giao diện chính
                mainHandler.post(() -> callback.onResult(reply));

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError("Không kết nối được Gemini API"));
            }
        }).start();
    }
}