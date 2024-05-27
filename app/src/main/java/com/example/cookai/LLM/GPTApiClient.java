package com.example.cookai.LLM;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import java.io.IOException;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GPTApiClient {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = ""; // Replace with your actual API key
    private final OkHttpClient client;
    private final SharedPreferences sharedPreferences;

    public GPTApiClient(Context context) {
        // Create a logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d("OkHttp", message);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Build the OkHttpClient with the logging interceptor
        client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        // Initialize SharedPreferences
        sharedPreferences = context.getSharedPreferences("com.example.cookai", Context.MODE_PRIVATE);
    }

    public String getRecipeSuggestions(String prompt) throws IOException, JSONException {

        return getRecipeSuggestions(prompt, true);
    }

    public String getRecipeSuggestions(String prompt, boolean decreaseprompt) throws IOException, JSONException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        // Construct JSON payload using JSONObject
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("model", "gpt-3.5-turbo");

        // Construct messages array
        JSONArray messagesArray = new JSONArray();
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant.");
        messagesArray.put(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messagesArray.put(userMessage);

        jsonPayload.put("messages", messagesArray);
        jsonPayload.put("max_tokens", 300);

        // Ensure correct JSON encoding
        RequestBody body = RequestBody.create(jsonPayload.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY) // Ensure this is correct
                .post(body)
                .build();

        // Log the headers to verify the API key is included
        Log.d("GPTApiClient", "Request Headers: " + request.headers().toString());

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // Decrease the prompt count
            if (decreaseprompt)
                decreasePromptCount();
            return response.body().string();
        }
    }

    private void decreasePromptCount() {
        int promptsLeft = sharedPreferences.getInt("prompts_left", 100);
        promptsLeft--;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("prompts_left", promptsLeft);
        editor.apply();
    }
}
