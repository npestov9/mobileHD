package com.example.cookai.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saved_recipes")
public class SavedRecipe {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String recipe;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getRecipe() { return recipe; }
    public void setRecipe(String recipe) { this.recipe = recipe; }
}
