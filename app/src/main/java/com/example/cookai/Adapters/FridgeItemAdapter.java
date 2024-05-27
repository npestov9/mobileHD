package com.example.cookai.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cookai.Model.FridgeItem;
import com.example.cookai.Model.FridgeItemDao;
import com.example.cookai.R;

import java.util.List;

public class FridgeItemAdapter extends RecyclerView.Adapter<FridgeItemAdapter.ViewHolder> {
    private final List<FridgeItem> fridgeItems;
    private final FridgeItemDao fridgeItemDao;
    private final Context context;

    public FridgeItemAdapter(Context context, List<FridgeItem> fridgeItems, FridgeItemDao fridgeItemDao) {
        this.context = context;
        this.fridgeItems = fridgeItems;
        this.fridgeItemDao = fridgeItemDao;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fridge_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FridgeItem item = fridgeItems.get(position);
        holder.ingredientTextView.setText(item.getIngredient() + " (" + item.getQuantity() + " " + item.getUnit() + ")");
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the item from the database
                fridgeItemDao.delete(item);
                // Remove the item from the list and notify the adapter
                fridgeItems.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return fridgeItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientTextView;
        Button deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            ingredientTextView = itemView.findViewById(R.id.ingredientTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
