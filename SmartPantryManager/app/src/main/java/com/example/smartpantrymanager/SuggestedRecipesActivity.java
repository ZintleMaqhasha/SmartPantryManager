package com.example.smartpantrymanager;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ListView listRecipes;
    private TextView textNoRecipes;
    private Button btnBackToPantry;
    private ArrayList<String> recipes;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        listRecipes = findViewById(R.id.listRecipes);
        textNoRecipes = findViewById(R.id.textNoRecipes);
        btnBackToPantry = findViewById(R.id.btnBackToPantry);

        databaseHelper = new DatabaseHelper(this);
        recipes = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                recipes
        );

        listRecipes.setAdapter(adapter);
        btnBackToPantry.setOnClickListener(v -> finish());

        loadRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && recipes != null) {
            loadRecipes();
        }
    }

    private void loadRecipes() {
        recipes.clear();

        try (Cursor cursor = databaseHelper.getSuggestedRecipes()) {
            int idColumn = cursor.getColumnIndexOrThrow("id");
            int nameColumn = cursor.getColumnIndexOrThrow("name");
            int totalColumn = cursor.getColumnIndexOrThrow("total_ingredients");
            int availableColumn = cursor.getColumnIndexOrThrow("available_ingredients");

            while (cursor.moveToNext()) {
                int recipeId = cursor.getInt(idColumn);
                String name = cursor.getString(nameColumn);
                int total = cursor.getInt(totalColumn);
                int available = cursor.getInt(availableColumn);

                String details =
                        databaseHelper.getRecipeIngredientDetails(recipeId);

                recipes.add(name + "\n"
                        + "Ingredients available: " + available + " / " + total
                        + "\n" + details);
            }
        }

        adapter.notifyDataSetChanged();

        if (recipes.isEmpty()) {
            textNoRecipes.setVisibility(View.VISIBLE);
            listRecipes.setVisibility(View.GONE);
            textNoRecipes.setText(R.string.no_recipes);
        } else {
            textNoRecipes.setVisibility(View.GONE);
            listRecipes.setVisibility(View.VISIBLE);
        }
    }
}