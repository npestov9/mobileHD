package com.example.cookai.Model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface FridgeItemDao {
    @Query("SELECT * FROM fridge_items")
    List<FridgeItem> getAll();

    @Query("SELECT * FROM fridge_items WHERE ingredient = :ingredient LIMIT 1")
    FridgeItem getByIngredient(String ingredient);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(FridgeItem fridgeItem);

    @Update
    void update(FridgeItem fridgeItem);

    @Query("UPDATE fridge_items SET quantity = quantity + :quantity WHERE ingredient = :ingredient")
    void updateQuantity(String ingredient, int quantity);

    @Delete
    void delete(FridgeItem fridgeItem);
}
