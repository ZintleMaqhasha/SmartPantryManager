
package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PantryActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ListView listPantry;
    private Button btnAddPantryIngredient;

    private final ArrayList<String> ingredients = new ArrayList<>();
    private final ArrayList<Integer> ingredientIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        databaseHelper = new DatabaseHelper(this);

        listPantry = findViewById(R.id.listPantry);
        btnAddPantryIngredient = findViewById(R.id.btnAddPantryIngredient);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                ingredients
        );

        listPantry.setAdapter(adapter);

        btnAddPantryIngredient.setOnClickListener(v -> {
            Intent intent = new Intent(
                    PantryActivity.this,
                    ActivityIngredientActivity.class
            );
            startActivity(intent);
        });

        listPantry.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= ingredientIds.size()) {
                return;
            }

            int selectedId = ingredientIds.get(position);
            String selectedName = ingredients.get(position);

            String[] options = {"Edit", "Delete"};

            new AlertDialog.Builder(PantryActivity.this)
                    .setTitle(selectedName)
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(
                                    PantryActivity.this,
                                    ActivityIngredientActivity.class
                            );
                            intent.putExtra("ingredient_id", selectedId);
                            startActivity(intent);
                        } else {
                            confirmDelete(selectedId);
                        }
                    })
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && adapter != null) {
            loadIngredients();
        }
    }

    private void loadIngredients() {
        ingredients.clear();
        ingredientIds.clear();

        try (Cursor cursor = databaseHelper.getAllIngredients()) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));

                double quantity = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_QUANTITY));

                String unit = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UNIT));

                ingredientIds.add(id);
                ingredients.add(name + " - " + quantity + " " + unit);
            }
        }

        if (ingredients.isEmpty()) {
            ingredients.add("Your pantry is empty. Add an ingredient to get started.");
        }

        adapter.notifyDataSetChanged();
    }

    private void confirmDelete(int ingredientId) {
        new AlertDialog.Builder(PantryActivity.this)
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to delete this ingredient?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = databaseHelper.deleteIngredient(ingredientId);

                    if (result > 0) {
                        Toast.makeText(
                                PantryActivity.this,
                                "Ingredient deleted!",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadIngredients();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
