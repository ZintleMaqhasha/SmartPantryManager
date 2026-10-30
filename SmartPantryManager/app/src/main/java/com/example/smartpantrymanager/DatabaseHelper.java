package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartPantry.db";
    private static final int DATABASE_VERSION = 5;

    public static final String TABLE_PANTRY = "pantry";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";

    private static final String TABLE_RECIPES = "recipes";
    private static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    private static final String TABLE_SHOPPING = "shopping_list";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createPantryTable(db);
        createRecipesTable(db);
        createRecipeIngredientsTable(db);
        createShoppingTable(db);
        insertSampleRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createPantryTable(db);
        createRecipesTable(db);
        createRecipeIngredientsTable(db);
        createShoppingTable(db);

        if (oldVersion < 4) {
            insertSampleRecipes(db);
        }
    }

    private void createPantryTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_PANTRY + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_QUANTITY + " REAL NOT NULL, " +
                COL_UNIT + " TEXT NOT NULL)";
        db.execSQL(sql);
    }

    private void createRecipesTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE)";
        db.execSQL(sql);
    }

    private void createRecipeIngredientsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPE_INGREDIENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "ingredient_name TEXT NOT NULL, " +
                "FOREIGN KEY(recipe_id) REFERENCES " + TABLE_RECIPES + "(id), " +
                "UNIQUE(recipe_id, ingredient_name))";
        db.execSQL(sql);
    }

    private void createShoppingTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_SHOPPING + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "quantity TEXT NOT NULL DEFAULT '', " +
                "is_checked INTEGER NOT NULL DEFAULT 0)";
        db.execSQL(sql);
    }

    private void insertSampleRecipes(SQLiteDatabase db) {
        String[][] recipes = {
                {"Vegetable Fried Rice", "rice", "vegetables", "egg"},
                {"Tomato Pasta", "pasta", "tomato", "onion"},
                {"Cheese Omelette", "egg", "cheese"},
                {"Potato Soup", "potato", "onion", "milk"},
                {"Chicken Stir-Fry", "chicken", "vegetables", "soy sauce"},
                {"Vegetable Soup", "vegetables", "potato", "carrot"},
                {"Pancakes", "flour", "egg", "milk"},
                {"French Toast", "bread", "egg", "milk"},
                {"Tuna Sandwich", "bread", "tuna", "mayonnaise"},
                {"Chicken Salad", "chicken", "lettuce", "tomato"},
                {"Garlic Bread", "bread", "garlic", "butter"},
                {"Mashed Potatoes", "potato", "milk", "butter"},
                {"Tomato Omelette", "egg", "tomato"},
                {"Rice and Beans", "rice", "beans"},
                {"Cheese Sandwich", "bread", "cheese"}
        };

        for (String[] recipe : recipes) {
            ContentValues recipeValues = new ContentValues();
            recipeValues.put("name", recipe[0]);

            db.insertWithOnConflict(
                    TABLE_RECIPES, null, recipeValues,
                    SQLiteDatabase.CONFLICT_IGNORE
            );

            try (Cursor cursor = db.query(
                    TABLE_RECIPES,
                    new String[]{"id"},
                    "name=?",
                    new String[]{recipe[0]},
                    null, null, null)) {

                if (cursor.moveToFirst()) {
                    int recipeId = cursor.getInt(0);

                    for (int i = 1; i < recipe.length; i++) {
                        ContentValues ingredientValues = new ContentValues();
                        ingredientValues.put("recipe_id", recipeId);
                        ingredientValues.put("ingredient_name", recipe[i]);

                        db.insertWithOnConflict(
                                TABLE_RECIPE_INGREDIENTS,
                                null,
                                ingredientValues,
                                SQLiteDatabase.CONFLICT_IGNORE
                        );
                    }
                }
            }
        }
    }

    public Cursor getSuggestedRecipes() {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT r.id, r.name, " +
                "(SELECT COUNT(*) FROM " + TABLE_RECIPE_INGREDIENTS +
                " ri WHERE ri.recipe_id = r.id) AS total_ingredients, " +
                "(SELECT COUNT(DISTINCT ri.ingredient_name) FROM " +
                TABLE_RECIPE_INGREDIENTS + " ri INNER JOIN " + TABLE_PANTRY +
                " p ON LOWER(p.name) LIKE '%' || LOWER(ri.ingredient_name) || '%' " +
                "WHERE ri.recipe_id = r.id) AS available_ingredients " +
                "FROM " + TABLE_RECIPES + " r " +
                "ORDER BY available_ingredients DESC, r.name ASC";

        return db.rawQuery(sql, null);
    }

    public String getRecipeIngredientDetails(int recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder details = new StringBuilder();

        String sql = "SELECT ri.ingredient_name, " +
                "CASE WHEN EXISTS (" +
                "SELECT 1 FROM " + TABLE_PANTRY + " p " +
                "WHERE LOWER(p.name) LIKE '%' || LOWER(ri.ingredient_name) || '%' " +
                ") THEN 1 ELSE 0 END AS available " +
                "FROM " + TABLE_RECIPE_INGREDIENTS + " ri " +
                "WHERE ri.recipe_id = ? " +
                "ORDER BY ri.ingredient_name";

        try (Cursor cursor = db.rawQuery(
                sql, new String[]{String.valueOf(recipeId)})) {

            while (cursor.moveToNext()) {
                String ingredient = cursor.getString(0);
                boolean available = cursor.getInt(1) == 1;

                details.append(available ? "✓ " : "✗ ")
                        .append(ingredient)
                        .append(available ? " — Available" : " — Missing")
                        .append("\n");
            }
        }

        return details.toString().trim();
    }

    public long addIngredient(String name, double quantity, String unit) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_QUANTITY, quantity);
        values.put(COL_UNIT, unit);

        return db.insert(TABLE_PANTRY, null, values);
    }

    public Cursor getAllIngredients() {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_PANTRY + " ORDER BY " + COL_NAME,
                null
        );
    }

    public int updateIngredient(int id, String name, double quantity, String unit) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_QUANTITY, quantity);
        values.put(COL_UNIT, unit);

        return db.update(
                TABLE_PANTRY, values, COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    public int deleteIngredient(int id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_PANTRY, COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    public Cursor getIngredientById(int id) {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_PANTRY, null, COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );
    }

    public long addShoppingItem(String name, String quantity) {
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity.trim());
        values.put("is_checked", 0);

        return getWritableDatabase().insert(TABLE_SHOPPING, null, values);
    }

    public Cursor getShoppingItems() {
        return getReadableDatabase().query(
                TABLE_SHOPPING,
                new String[]{"id", "name", "quantity", "is_checked"},
                null, null, null, null,
                "is_checked ASC, name ASC"
        );
    }

    public int updateShoppingItem(int id, String name, String quantity) {
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity.trim());

        return getWritableDatabase().update(
                TABLE_SHOPPING, values, "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    public int setShoppingItemChecked(int id, boolean checked) {
        ContentValues values = new ContentValues();
        values.put("is_checked", checked ? 1 : 0);

        return getWritableDatabase().update(
                TABLE_SHOPPING, values, "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    public int deleteShoppingItem(int id) {
        return getWritableDatabase().delete(
                TABLE_SHOPPING, "id=?",
                new String[]{String.valueOf(id)}
        );
    }
}
