
package com.example.smartpantrymanager;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityIngredientActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private EditText editIngredientName, editQuantity, editUnit;
    private Button btnSaveIngredient;
    private int ingredientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient);

        databaseHelper = new DatabaseHelper(this);

        editIngredientName = findViewById(R.id.editIngredientName);
        editQuantity = findViewById(R.id.editQuantity);
        editUnit = findViewById(R.id.editUnit);
        btnSaveIngredient = findViewById(R.id.btnSaveIngredient);

        ingredientId = getIntent().getIntExtra("ingredient_id", -1);

        if (ingredientId != -1) {
            loadIngredient();
            btnSaveIngredient.setText("Update Ingredient");
        }

        btnSaveIngredient.setOnClickListener(v -> saveIngredient());
    }

    private void loadIngredient() {
        try (Cursor cursor = databaseHelper.getIngredientById(ingredientId)) {
            if (cursor != null && cursor.moveToFirst()) {
                editIngredientName.setText(cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME)));

                editQuantity.setText(String.valueOf(cursor.getDouble(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_QUANTITY))));

                editUnit.setText(cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UNIT)));
            } else {
                Toast.makeText(this, "Ingredient not found",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void saveIngredient() {
        String name = editIngredientName.getText().toString().trim();
        String quantityText = editQuantity.getText().toString().trim();
        String unit = editUnit.getText().toString().trim();

        if (name.isEmpty() || quantityText.isEmpty() || unit.isEmpty()) {
            Toast.makeText(this, "Please complete all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        double quantity;

        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid quantity",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantity <= 0) {
            Toast.makeText(this, "Quantity must be greater than zero",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (ingredientId == -1) {
            long result = databaseHelper.addIngredient(name, quantity, unit);

            if (result != -1) {
                Toast.makeText(this, "Ingredient saved successfully",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Could not save ingredient",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int result = databaseHelper.updateIngredient(
                    ingredientId, name, quantity, unit);

            if (result > 0) {
                Toast.makeText(this, "Ingredient updated successfully",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Could not update ingredient",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
