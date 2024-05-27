package com.example.cookai;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.cookai.Adapters.FridgeItemAdapter;
import com.example.cookai.Model.AppDatabase;
import com.example.cookai.Model.FridgeItem;
import com.example.cookai.Model.FridgeItemDao;

import java.util.List;

public class FridgeActivity extends AppCompatActivity {
    private RecyclerView ingredientRecyclerView;
    private EditText ingredientEditText;
    private EditText quantityEditText;
    private Spinner unitSpinner;
    private Button addIngredientButton;
    private FridgeItemDao fridgeItemDao;
    private FridgeItemAdapter adapter;
    private List<FridgeItem> fridgeItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fridge);

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "database-name")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
        fridgeItemDao = db.fridgeItemDao();

        ingredientRecyclerView = findViewById(R.id.ingredientRecyclerView);
        ingredientEditText = findViewById(R.id.ingredientEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        unitSpinner = findViewById(R.id.unitSpinner);
        addIngredientButton = findViewById(R.id.addIngredientButton);

        // Load all fridge items from the database
        fridgeItems = fridgeItemDao.getAll();
        adapter = new FridgeItemAdapter(this, fridgeItems, fridgeItemDao);
        ingredientRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ingredientRecyclerView.setAdapter(adapter);

        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newIngredient = ingredientEditText.getText().toString().trim();
                String quantityStr = quantityEditText.getText().toString().trim();
                String unit = unitSpinner.getSelectedItem().toString();
                if (!newIngredient.isEmpty() && !quantityStr.isEmpty()) {
                    int quantity = Integer.parseInt(quantityStr);
                    FridgeItem fridgeItem = new FridgeItem();
                    fridgeItem.setIngredient(newIngredient);
                    fridgeItem.setQuantity(quantity);
                    fridgeItem.setUnit(unit);
                    fridgeItemDao.insert(fridgeItem);

                    // Refresh the list
                    fridgeItems.clear();
                    fridgeItems.addAll(fridgeItemDao.getAll());
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
}
