package com.example.cookai.Model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GroceryItemDao {
    @Query("SELECT * FROM grocery_items")
    List<GroceryItem> getAll();

    @Query("SELECT * FROM grocery_items WHERE ingredient = :ingredient AND unit = :unit LIMIT 1")
    GroceryItem getByIngredientAndUnit(String ingredient, String unit);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(GroceryItem groceryItem);

    @Update
    void update(GroceryItem groceryItem);

    @Query("UPDATE grocery_items SET quantity = quantity + :quantity WHERE ingredient = :ingredient AND unit = :unit")
    void updateQuantity(String ingredient, int quantity, String unit);

    @Delete
    void delete(GroceryItem groceryItem);
}
