package com.example.cookai.Model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SavedRecipeDao {
    @Query("SELECT * FROM saved_recipes")
    List<SavedRecipe> getAll();

    @Insert
    void insert(SavedRecipe savedRecipe);

    @Query("DELETE FROM saved_recipes WHERE id = :id")
    void deleteById(int id);
}
