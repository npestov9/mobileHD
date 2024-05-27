package com.example.cookai;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.cookai.LLM.GPTApiClient;
import com.example.cookai.Model.AppDatabase;
import com.example.cookai.Model.FridgeItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private ImageButton fridgeListButton;
    private ImageButton groceryListButton;
    private Button whatCanIMakeButton;
    private Button promptRecipeButton;
    private Button feelingLuckyButton;
    private ImageView profileIcon;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "database-name")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        fridgeListButton = findViewById(R.id.fridgeListButton);
        groceryListButton = findViewById(R.id.groceryListButton);
        whatCanIMakeButton = findViewById(R.id.whatCanIMakeButton);
        promptRecipeButton = findViewById(R.id.promptRecipeButton);
        feelingLuckyButton = findViewById(R.id.randomRecipeButton);
        profileIcon = findViewById(R.id.profileIcon);

        fridgeListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, FridgeActivity.class);
                startActivity(intent);
            }
        });

        groceryListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, GroceryActivity.class);
                startActivity(intent);
            }
        });

        whatCanIMakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
                intent.putExtra("ingredients", ingredientsList.toString());
                startActivity(intent);
            }
        });

        promptRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RecipePromptActivity.class);
                startActivity(intent);
            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        feelingLuckyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRandomRecipes();
            }
        });
    }

    private void generateRandomRecipes() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GPTApiClient apiClient = new GPTApiClient(HomeActivity.this);
                try {
                    String response = apiClient.getRecipeSuggestions("Give me three random recipes.");
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject messageObj = choice.getJSONObject("message");
                    String assistantMessage = messageObj.getString("content");

                    runOnUiThread(() -> showRandomRecipes(assistantMessage));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showRandomRecipes(String recipes) {
        new AlertDialog.Builder(this)
                .setTitle("Feeling Lucky?")
                .setMessage("Here are some random recipes:\n\n" + recipes)
                .setPositiveButton("OK", null)
                .show();
    }
}
