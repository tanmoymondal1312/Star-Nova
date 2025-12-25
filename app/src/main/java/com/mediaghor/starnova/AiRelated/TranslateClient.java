package com.mediaghor.starnova.AiRelated;

import android.content.Context;

import com.mediaghor.starnova.R;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.IOException;

public class TranslateClient {

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient();

    public static void translate(
            Context context,
            String text,
            String source,
            String target,
            TranslateCallback callback
    ) {

        try {
            // Get the base URL from string resources
            String baseUrl = context.getString(R.string.SERVER_URL);
            String apiUrl = baseUrl + "/api/translate";

            JSONObject json = new JSONObject();
            json.put("text", text);
            json.put("source", source);
            json.put("target", target);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .build();

            // 🔥 Runs automatically on background thread
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (!response.isSuccessful()) {
                        callback.onError("HTTP Error: " + response.code());
                        return;
                    }

                    String responseBody = response.body().string();

                    try {
                        JSONObject result = new JSONObject(responseBody);
                        String translatedText = result.optString("translated");
                        callback.onSuccess(translatedText);
                    } catch (Exception e) {
                        callback.onError("Invalid JSON response");
                    }
                }
            });

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}