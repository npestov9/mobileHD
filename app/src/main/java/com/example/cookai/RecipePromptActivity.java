package com.example.cookai;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.example.cookai.LLM.GPTApiClient;
import com.example.cookai.Model.AppDatabase;
import com.example.cookai.Model.FridgeItem;
import com.example.cookai.Model.GroceryItem;
import com.example.cookai.Model.GroceryItemDao;
import com.example.cookai.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecipePromptActivity extends AppCompatActivity {
    private EditText recipePromptEditText;
    private Button checkIngredientsButton;
    private ListView recipeResponseListView;
    private ArrayList<String> messages;
    private ArrayAdapter<String> adapter;
    private AppDatabase db;
    private GroceryItemDao groceryItemDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_prompt);

        recipePromptEditText = findViewById(R.id.recipePromptEditText);
        checkIngredientsButton = findViewById(R.id.checkIngredientsButton);
        recipeResponseListView = findViewById(R.id.recipeResponseListView);

        messages = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        recipeResponseListView.setAdapter(adapter);

        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "database-name")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
        groceryItemDao = db.groceryItemDao();

        // Check if there's a recipe prompt passed through the intent
        Intent intent = getIntent();
        String recipePrompt = intent.getStringExtra("recipe_prompt");

        if (recipePrompt != null && !recipePrompt.trim().isEmpty()) {
            messages.add("You: " + recipePrompt);
            adapter.notifyDataSetChanged();
            getRecipeAndCheckIngredients(recipePrompt);
        }

        checkIngredientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipePrompt = recipePromptEditText.getText().toString().trim();
                if (!recipePrompt.isEmpty()) {
                    messages.add("You: " + recipePrompt);
                    adapter.notifyDataSetChanged();
                    getRecipeAndCheckIngredients(recipePrompt);
                }
            }
        });
    }

    private void getRecipeAndCheckIngredients(String recipePrompt) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GPTApiClient apiClient = new GPTApiClient(RecipePromptActivity.this);
                try {
                    // Fetch ingredients from the database
                    List<FridgeItem> fridgeItems = db.fridgeItemDao().getAll();
                    StringBuilder ingredientsList = new StringBuilder();
                    for (FridgeItem item : fridgeItems) {
                        ingredientsList.append(item.getIngredient())
                                .append(" (")
                                .append(item.getQuantity())
                                .append(" ")
                                .append(item.getUnit())
                                .append("), ");
                    }

                    if (ingredientsList.length() > 0) {
                        ingredientsList.setLength(ingredientsList.length() - 2); // Remove trailing comma and space
                    }

                    // Get recipe suggestion from GPT
                    String recipeResponse = apiClient.getRecipeSuggestions("Give a detailed recipe instruction with a list of ingredients for: " + recipePrompt + ". Please keep it simple under 150 words");
                    JSONObject recipeJsonResponse = new JSONObject(recipeResponse);
                    JSONArray recipeChoices = recipeJsonResponse.getJSONArray("choices");
                    JSONObject recipeChoice = recipeChoices.getJSONObject(0);
                    JSONObject recipeMessageObj = recipeChoice.getJSONObject("message");
                    String recipe = recipeMessageObj.getString("content");

                    // Display recipe suggestion
                    runOnUiThread(() -> {
                        messages.add("Recipe Suggestion: " + recipe);
                        adapter.notifyDataSetChanged();
                    });

                    // Check for missing ingredients
                    String checkIngredientsPrompt = "Here is a recipe I would like to make:\n\n" + recipe + "\n\nHere are the ingredients I currently have in my fridge:\n" + ingredientsList.toString() + "\n\nList any missing ingredients I need to buy to complete this recipe. Use the following format for each missing ingredient: 'INGREDIENT, QUANTITY, QUANTITY TYPE (count or grams)'. Only include ingredients that are missing from my fridge list. The format is extremely important.";
                    String missingIngredientsResponse = apiClient.getRecipeSuggestions(checkIngredientsPrompt);
                    JSONObject missingIngredientsJsonResponse = new JSONObject(missingIngredientsResponse);
                    JSONArray missingIngredientsChoices = missingIngredientsJsonResponse.getJSONArray("choices");
                    JSONObject missingIngredientsChoice = missingIngredientsChoices.getJSONObject(0);
                    JSONObject missingIngredientsMessageObj = missingIngredientsChoice.getJSONObject("message");
                    String missingIngredients = missingIngredientsMessageObj.getString("content");

                    // Display missing ingredients
                    runOnUiThread(() -> displayMissingIngredients(missingIngredients));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        messages.add("Error: " + e.getMessage());
                        adapter.notifyDataSetChanged();
                    });
                }
            }
        }).start();
    }

    private void displayMissingIngredients(String missingIngredients) {
        new AlertDialog.Builder(this)
                .setTitle("Missing Ingredients")
                .setMessage("You are missing the following ingredients:\n" + missingIngredients)
                .setPositiveButton("Add to Shopping List", (dialog, which) -> {
                    // Add missing ingredients to shopping list
                    String[] lines = missingIngredients.split("\n");
                    for (String line : lines) {
                        if (!line.trim().isEmpty()) {
                            String[] parts = line.split(", ");
                            if (parts.length == 3) {
                                String ingredient = parts[0];
//                                int quantity = Integer.parseInt(parts[1]);
//                                String unit = parts[2];

                                // Add to shopping list database

                                    GroceryItem groceryItem = new GroceryItem();
                                    groceryItem.setIngredient(line);
                                    groceryItem.setQuantity(0);
                                    groceryItem.setUnit("");
                                    groceryItemDao.insert(groceryItem);

                            }
                        }
                    }
                    messages.add("Added missing ingredients to shopping list.");
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    messages.add("Recipe declined due to missing ingredients.");
                    adapter.notifyDataSetChanged();
                })
                .show();
    }
}
