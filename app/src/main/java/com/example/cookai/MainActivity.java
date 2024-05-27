package com.example.cookai;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cookai.Model.FridgeItem;
import com.example.cookai.Model.FridgeItemDao;
import com.example.cookai.Model.GroceryItem;
import com.example.cookai.Model.GroceryItemDao;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FridgeItemDao fridgeItemDao;
    private GroceryItemDao groceryItemDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fridgeItemDao = MyApp.getDatabase().fridgeItemDao();
        groceryItemDao = MyApp.getDatabase().groceryItemDao();

        // Example usage
        addFridgeItem("Milk", 2);
        addGroceryItem("Eggs", 12);
        List<FridgeItem> fridgeItems = getAllFridgeItems();
        List<GroceryItem> groceryItems = getAllGroceryItems();

        Log.d("MainActivity", "Fridge Items: " + fridgeItems);
        Log.d("MainActivity", "Grocery Items: " + groceryItems);
    }

    private void addFridgeItem(String ingredient, int quantity) {
        FridgeItem item = new FridgeItem();
        item.setIngredient(ingredient);
        item.setQuantity(quantity);
        fridgeItemDao.insert(item);
    }

    private void addGroceryItem(String ingredient, int quantity) {
        GroceryItem item = new GroceryItem();
        item.setIngredient(ingredient);
        item.setQuantity(quantity);
        groceryItemDao.insert(item);
    }

    private List<FridgeItem> getAllFridgeItems() {
        return fridgeItemDao.getAll();
    }

    private List<GroceryItem> getAllGroceryItems() {
        return groceryItemDao.getAll();
    }


}
