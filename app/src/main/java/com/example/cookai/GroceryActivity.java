package com.example.cookai;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.example.cookai.Model.AppDatabase;
import com.example.cookai.Model.FridgeItem;
import com.example.cookai.Model.GroceryItem;
import com.example.cookai.Model.GroceryItemDao;

import java.util.ArrayList;
import java.util.List;

public class GroceryActivity extends AppCompatActivity {
    private ArrayList<String> groceryItems;
    private ArrayAdapter<String> adapter;
    private AppDatabase db;
    private GroceryItemDao groceryItemDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery);

        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "database-name")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
        groceryItemDao = db.groceryItemDao();

        List<GroceryItem> items = groceryItemDao.getAll();
        groceryItems = new ArrayList<>();
        for (GroceryItem item : items) {
            String quant = String.valueOf(item.getQuantity());
            if (quant.equals("0"))
                groceryItems.add(item.getIngredient());
            else
                groceryItems.add(item.getIngredient() + " (" + quant + " " + item.getUnit() + ")");
        }

        adapter = new ArrayAdapter<>(this, R.layout.grocery_list_item, R.id.groceryItemText, groceryItems);

        ListView groceryListView = findViewById(R.id.groceryListView);
        groceryListView.setAdapter(adapter);

        groceryListView.setOnItemClickListener((parent, view, position, id) -> {
            moveToFridge(position);
        });
    }

    private void updateGroceryList() {
        groceryItems.clear();
        List<GroceryItem> updatedGroceryItems = groceryItemDao.getAll();
        for (GroceryItem item : updatedGroceryItems) {
            groceryItems.add(item.getIngredient() + " (" + item.getQuantity() + " " + item.getUnit() + ")");
        }
        adapter.notifyDataSetChanged();
    }

    private void moveToFridge(int position) {
        String item = groceryItems.get(position);
        String[] parts = item.split(" \\(");
        String ingredient = parts[0].trim();
        String quantity = "";
        String unit = "";

        if (parts.length == 2) {
            String[] quantityUnit = parts[1].replace(")", "").split(" ");
            if (quantityUnit.length == 2) {
                quantity = quantityUnit[0];
                unit = quantityUnit[1].trim();
            }
        }

        GroceryItem groceryItem = groceryItemDao.getByIngredientAndUnit(ingredient, unit);
        if (groceryItem == null && !unit.isEmpty()) {
            groceryItem = groceryItemDao.getByIngredientAndUnit(ingredient, "");
        }

        if (groceryItem != null) {
            FridgeItem fridgeItem = new FridgeItem();
            fridgeItem.setIngredient(groceryItem.getIngredient());
            if (!quantity.isEmpty()) {
                fridgeItem.setQuantity(Integer.parseInt(quantity));
            }
            fridgeItem.setUnit(unit);

            db.fridgeItemDao().insert(fridgeItem);
            groceryItemDao.delete(groceryItem);

            updateGroceryList();
            Toast.makeText(this, ingredient + " moved to fridge.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Item not found.", Toast.LENGTH_SHORT).show();
        }
    }
}
