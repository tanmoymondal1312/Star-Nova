package com.mediaghor.starnova.AiRelated;


import android.util.Log;

import com.mediaghor.starnova.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroqAIClint {

    private static final String TAG = "AI_RESPONSE";


    // 🔑 Replace with your Groq API key
    private static final String GROQ_API_KEY = BuildConfig.GROQ_API_KEY;

    private static final String BASE_URL = "https://api.groq.com/openai/v1/chat/completions";

    private static final OkHttpClient client = new OkHttpClient();

    public static void sendMessage(String userMessage, String mode, AiResponseCallback callback) {
        String systemContent = "";

        if (mode.equals("teacher")){

            systemContent =
                    "You are a kind English teacher for children.\n" +
                            "\n" +
                            "You MUST always follow this exact format:\n" +
                            "1–2 short lines of explanation in very simple English.\n" +
                            "Last line: ONE simple question wrapped exactly like this:\n" +
                            "(* question *)\n" +
                            "\n" +
                            "MANDATORY RULES:\n" +
                            "1. You MUST always include the question.\n" +
                            "2. The question MUST be the last line.\n" +
                            "3. Ask ONLY ONE question.\n" +
                            "4. The Question Must Like This (* question *)\n" +
                            "5. Use very simple English for kids.\n" +
                            "6. Never write anything after the question.\n" +
                            "7. Never skip the format.\n";
        } else if (mode.equals("translator")) {
             systemContent =
                    "*** STRICT TRANSLATION MODE (NO THINKING, NO ANSWERS) ***\n" +
                            "ROLE: You are a machine translator.\n" +
                            "TASK: Convert English text to Bangla (Bengali).\n" +
                            "\n" +
                            "RULES (MUST FOLLOW EXACTLY):\n" +
                            "1. Translate the input text WORD-BY-WORD into Bangla.\n" +
                            "2. DO NOT answer questions. DO NOT respond conversationally.\n" +
                            "3. DO NOT explain, summarize, rephrase, or improve the text.\n" +
                            "4. DO NOT add or remove any meaning.\n" +
                            "5. Preserve the original sentence structure and intent.\n" +
                            "6. Output ONLY the Bangla translation.\n" +
                            "7. If the input is a question, translate it as a question — DO NOT answer it.\n" +
                            "8. No emojis, no punctuation changes, no extra words.\n" +
                            "9. No English text in the output.\n";
        }
        try {
            // ---------- SYSTEM MESSAGE ----------
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemContent);


            // ---------- USER MESSAGE ----------
            JSONObject userMessageObj = new JSONObject();
            userMessageObj.put("role", "user");
            userMessageObj.put("content", userMessage);

            // ---------- MESSAGES ARRAY ----------
            JSONArray messagesArray = new JSONArray();
            messagesArray.put(systemMessage);
            messagesArray.put(userMessageObj);

            // ---------- REQUEST BODY ----------
            JSONObject json = new JSONObject();
            json.put("model", "llama-3.1-8b-instant");
            json.put("messages", messagesArray);
            json.put("temperature", 1);
            json.put("max_tokens", 1024);
            json.put("top_p", 1);
            json.put("stream", false);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            // ---------- REQUEST ----------
            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .addHeader("Authorization", "Bearer " + GROQ_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            // ---------- API CALL ----------
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        if (callback != null) {
                            callback.onFailure("HTTP Error: " + response.code());
                        }
                        return;
                    }

                    try {
                        String raw = response.body().string();
                        JSONObject obj = new JSONObject(raw);

                        String aiMessage = obj
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        if (callback != null) {
                            callback.onSuccess(aiMessage);
                        }

                    } catch (Exception e) {
                        if (callback != null) {
                            callback.onFailure(e.getMessage());
                        }
                    }
                }
            });

        } catch (Exception e) {
            if (callback != null) {
                callback.onFailure(e.getMessage());
            }
        }
    }
}
