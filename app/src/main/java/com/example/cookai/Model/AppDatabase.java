package com.example.cookai.Model;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {FridgeItem.class, GroceryItem.class, SavedRecipe.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FridgeItemDao fridgeItemDao();
    public abstract GroceryItemDao groceryItemDao();
    public abstract SavedRecipeDao savedRecipeDao();
}
