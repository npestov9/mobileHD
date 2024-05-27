package com.example.cookai.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fridge_items")
public class FridgeItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String ingredient;
    private int quantity;
    private String unit; // New field for unit

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getIngredient() { return ingredient; }
    public void setIngredient(String ingredient) { this.ingredient = ingredient; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
