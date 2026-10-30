
package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnAddIngredient, btnRecipes, btnShoppingList, btnPantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnRecipes = findViewById(R.id.btnRecipes);
        btnShoppingList = findViewById(R.id.btnShoppingList);
        btnPantry = findViewById(R.id.btnPantry);

        btnAddIngredient.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ActivityIngredientActivity.class
            );
            startActivity(intent);
        });

        btnRecipes.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SuggestedRecipesActivity.class
            );
            startActivity(intent);
        });

        btnShoppingList.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ShoppingListActivity.class
            );
            startActivity(intent);
        });

        btnPantry.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PantryActivity.class);
            startActivity(intent);
        });
    }
}
