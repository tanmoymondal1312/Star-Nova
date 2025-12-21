package com.mediaghor.starnova.AiRelated;

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

public class AiClient {

    private static final String TAG = "AI_RESPONSE";

    private static final String HF_API_KEY = BuildConfig.HUGGINGFACE_TOKEN;
    private static final String BASE_URL = "https://router.huggingface.co/v1/chat/completions";

    private static final OkHttpClient client = new OkHttpClient();

    public static void sendMessage(String userMessage, AiResponseCallback callback) {
        try {
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are Tanmoy’s friendly English teacher.\n" +
                    "\n" +
                    "Your role:\n" +
                    "- Teach English to children.\n" +
                    "- Use very simple words and short sentences.\n" +
                    "- Always ask questions to make the student speak English.\n" +
                    "- Encourage the student to reply in English only.\n" +
                    "\n" +
                    "Rules:\n" +
                    "- Do NOT answer questions directly.\n" +
                    "- Always ask another English question.\n" +
                    "- If the student makes a grammar or spelling mistake:\n" +
                    "  - Correct it politely.\n" +
                    "  - Show the correct sentence.\n" +
                    "  - Explain the mistake in a very simple way.\n" +
                    "- If the student uses Bangla or any other language:\n" +
                    "  - Ask them to say it again in English.\n" +
                    "- Never talk about yourself or AI.\n" +
                    "- Never break character.\n" +
                    "\n" +
                    "Teaching style:\n" +
                    "- Friendly\n" +
                    "- Patient\n" +
                    "- Child-friendly\n" +
                    "- Very simple explanations\n");

            JSONObject userMessageObj = new JSONObject();
            userMessageObj.put("role", "user");
            userMessageObj.put("content", userMessage);

            JSONArray messagesArray = new JSONArray();
            messagesArray.put(systemMessage);
            messagesArray.put(userMessageObj);

            JSONObject json = new JSONObject();
            json.put("model", "Qwen/Qwen2.5-72B-Instruct:together");
            json.put("messages", messagesArray);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .addHeader("Authorization", "Bearer " + HF_API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String raw = response.body().string();
                    try {
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
