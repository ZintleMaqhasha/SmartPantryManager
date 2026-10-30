package com.example.smartpantrymanager;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ShoppingListActivity extends AppCompatActivity {

    private EditText edtName, edtQuantity;
    private ListView listShopping;
    private DatabaseHelper databaseHelper;

    private final ArrayList<String> items = new ArrayList<>();
    private final ArrayList<Integer> itemIds = new ArrayList<>();
    private final ArrayList<Boolean> checkedItems = new ArrayList<>();

    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        edtName = findViewById(R.id.edtShoppingName);
        edtQuantity = findViewById(R.id.edtShoppingQuantity);
        listShopping = findViewById(R.id.listShopping);
        Button btnAdd = findViewById(R.id.btnAddShoppingItem);

        databaseHelper = new DatabaseHelper(this);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                items
        );

        listShopping.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addItem());

        listShopping.setOnItemClickListener((parent, view, position, id) -> {
            int selectedId = itemIds.get(position);

            if (checkedItems.get(position)) {
                showItemOptions(selectedId, position);
            } else {
                databaseHelper.setShoppingItemChecked(selectedId, true);
                loadItems();
            }
        });

        listShopping.setOnItemLongClickListener((parent, view, position, id) -> {
            showItemOptions(itemIds.get(position), position);
            return true;
        });

        loadItems();
    }

    private void addItem() {
        String name = edtName.getText().toString().trim();
        String quantity = edtQuantity.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError("Enter an ingredient name");
            return;
        }

        long result = databaseHelper.addShoppingItem(name, quantity);

        if (result != -1) {
            edtName.setText("");
            edtQuantity.setText("");
            Toast.makeText(this, "Item added!", Toast.LENGTH_SHORT).show();
            loadItems();
        } else {
            Toast.makeText(this, "Could not add item", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadItems() {
        items.clear();
        itemIds.clear();
        checkedItems.clear();

        try (Cursor cursor = databaseHelper.getShoppingItems()) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String quantity = cursor.getString(cursor.getColumnIndexOrThrow("quantity"));
                boolean checked = cursor.getInt(
                        cursor.getColumnIndexOrThrow("is_checked")
                ) == 1;

                itemIds.add(id);
                checkedItems.add(checked);

                String display = (checked ? "✓ " : "□ ") + name;

                if (!quantity.isEmpty()) {
                    display += " (" + quantity + ")";
                }

                if (checked) {
                    display += " — Purchased";
                }

                items.add(display);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void showItemOptions(int itemId, int position) {
        String[] options = checkedItems.get(position)
                ? new String[]{"Mark as not purchased", "Delete"}
                : new String[]{"Mark as purchased", "Delete"};

        new AlertDialog.Builder(this)
                .setTitle(items.get(position))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        databaseHelper.setShoppingItemChecked(
                                itemId,
                                !checkedItems.get(position)
                        );
                        loadItems();
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("Delete item?")
                                .setMessage("Remove this item from your shopping list?")
                                .setPositiveButton("Delete", (d, w) -> {
                                    databaseHelper.deleteShoppingItem(itemId);
                                    loadItems();
                                    Toast.makeText(
                                            this,
                                            "Item deleted",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                })
                .show();
    }
}