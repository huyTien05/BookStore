package com.example.bookapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookapp.R;
import com.example.bookapp.adapters.CartAdapter;
import com.example.bookapp.database.CartDAO;
import com.example.bookapp.models.CartItem;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvTotalPrice;
    private View layoutEmpty;
    private CartAdapter adapter;
    private CartDAO cartDAO;
    private List<CartItem> cartItems;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        loadUserId();
        cartDAO = new CartDAO(this);
        setupRecyclerView();
        loadCartData();
    }

    private void initViews() {
        rvCart = findViewById(R.id.rvCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            if (cartItems == null || cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show();
            } else {
                performCheckout();
            }
        });
    }

    private void performCheckout() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getBookPrice() * item.getQuantity();
        }

        com.example.bookapp.database.OrderDAO orderDAO = new com.example.bookapp.database.OrderDAO(this);
        long orderId = orderDAO.placeOrder(userId, total, cartItems);

        if (orderId != -1) {
            cartDAO.clearCart(userId);
            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
            loadCartData(); // Refresh UI
        } else {
            Toast.makeText(this, "Có lỗi xảy ra khi đặt hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("USER_ID", -1);
    }

    private void setupRecyclerView() {
        rvCart.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadCartData() {
        cartItems = cartDAO.getCartByUser(userId);
        if (cartItems.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvCart.setVisibility(View.VISIBLE);
        }

        adapter = new CartAdapter(this, cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChange(CartItem item, int newQuantity) {
                cartDAO.updateQuantity(item.getId(), newQuantity);
                loadCartData(); // Reload to update UI and total
            }

            @Override
            public void onRemoveItem(CartItem item) {
                cartDAO.removeFromCart(item.getId());
                loadCartData();
            }
        });
        rvCart.setAdapter(adapter);
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getBookPrice() * item.getQuantity();
        }
        tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", total));
    }
}
