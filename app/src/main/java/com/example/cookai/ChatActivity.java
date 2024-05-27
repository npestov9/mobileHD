package com.example.cookai;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cookai.LLM.GPTApiClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    private ListView chatListView;
    private Button anotherRecipeButton;
    private ArrayList<String> messages;
    private ArrayAdapter<String> adapter;
    private String ingredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatListView = findViewById(R.id.chatListView);
        anotherRecipeButton = findViewById(R.id.anotherRecipeButton);

        messages = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.chat_list_item, R.id.chatTextView, messages);
        chatListView.setAdapter(adapter);

        // Get ingredients from intent
        ingredients = getIntent().getStringExtra("ingredients");
        if (ingredients != null && !ingredients.trim().isEmpty()) {
            // Add the prompt to the messages list
            messages.add("You: What can I make with my ingredients?");
            adapter.notifyDataSetChanged();

            // Automatically send the prompt to GPT
            sendMessageToGPT("I want you to be my cooking instructor. I have a list of ingredients in fridge and pantry and you should tell me what I can make using them." +
                    " If a recipe is missing even one ingredient - avoid using it in the recipe but if that's " +
                    "impossible then find another one, if there is no recipe with exclusively what's in my fridge or there is nothing listed after : at the end " +
                    "then say - Your fridge is empty!. Let's go to the store!. Here are the ingredients I have: " + ingredients);
        } else {
            // Inform the user that the fridge is empty
            messages.add("Cook Master: Your fridge is empty! Let's go to the store or add ingredients to your fridge.");
            adapter.notifyDataSetChanged();
        }

        anotherRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Resend the same prompt to get another recipe
                sendMessageToGPT("I want you to be my cooking instructor. I have a list of ingredients in fridge and pantry and you should tell me what I can make using them." +
                        " If a recipe is missing even one ingredient - avoid using it in the recipe but if that's " +
                        "impossible then find another one, if there is no recipe with exclusively what's in my fridge or there is nothing listed after : at the end " +
                        "then say - Your fridge is empty! Let's go to the store!. Here are the ingredients I have: " + ingredients);
            }
        });
    }

    private void sendMessageToGPT(String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GPTApiClient apiClient = new GPTApiClient(ChatActivity.this);
                try {
                    String response = apiClient.getRecipeSuggestions(message);
                    // Parse the JSON response to extract the assistant's message
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject messageObj = choice.getJSONObject("message");
                    String assistantMessage = messageObj.getString("content");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messages.add("Cook Master: " + assistantMessage);
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messages.add("Error: " + e.getMessage());
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();
    }
}
