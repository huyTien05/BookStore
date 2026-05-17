package com.example.bookapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.bookapp.models.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {
    private DatabaseHelper dbHelper;

    public CartDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public List<CartItem> getCartByUser(int userId) {
        List<CartItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT c.id, c.user_id, c.book_id, c.quantity, b.title, b.price, b.image " +
                     "FROM cart c JOIN books b ON c.book_id = b.id " +
                     "WHERE c.user_id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                list.add(new CartItem(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getString(4),
                    cursor.getDouble(5),
                    cursor.getString(6)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void addToCart(int userId, int bookId, int quantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Kiểm tra xem đã có sách này trong giỏ hàng chưa
        Cursor cursor = db.rawQuery("SELECT id, quantity FROM cart WHERE user_id = ? AND book_id = ?", 
                                   new String[]{String.valueOf(userId), String.valueOf(bookId)});
        
        if (cursor.moveToFirst()) {
            int currentQty = cursor.getInt(1);
            ContentValues values = new ContentValues();
            values.put("quantity", currentQty + quantity);
            db.update("cart", values, "id = ?", new String[]{String.valueOf(cursor.getInt(0))});
        } else {
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("book_id", bookId);
            values.put("quantity", quantity);
            db.insert("cart", null, values);
        }
        cursor.close();
    }

    public void updateQuantity(int cartId, int quantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (quantity <= 0) {
            db.delete("cart", "id = ?", new String[]{String.valueOf(cartId)});
        } else {
            ContentValues values = new ContentValues();
            values.put("quantity", quantity);
            db.update("cart", values, "id = ?", new String[]{String.valueOf(cartId)});
        }
    }

    public void removeFromCart(int cartId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("cart", "id = ?", new String[]{String.valueOf(cartId)});
    }
    
    public void clearCart(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("cart", "user_id = ?", new String[]{String.valueOf(userId)});
    }
}
