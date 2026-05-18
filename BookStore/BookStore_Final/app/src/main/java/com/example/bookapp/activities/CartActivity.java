package com.example.bookapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.R;
import com.example.bookapp.adapters.CartAdapter;
import com.example.bookapp.database.CartDAO;
import com.example.bookapp.database.OrderDAO;
import com.example.bookapp.models.CartItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
    private String userRole;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // 1. Tải thông tin người dùng TRƯỚC khi khởi tạo views
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("USER_ID", -1);
        userRole = prefs.getString("ROLE", "user");
        username = prefs.getString("USERNAME", null);

        initViews();
        cartDAO = new CartDAO(this);
        setupRecyclerView();
        loadCartData();
        setupNavigation();
    }

    private void initViews() {
        rvCart = findViewById(R.id.rvCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            if (cartItems == null || cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show();
            } else {
                // Thay đổi ở đây: Hiện thông báo xác nhận thay vì đặt hàng luôn
                showCheckoutConfirmationDialog();
            }
        });
    }

    private void showCheckoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt hàng")
                .setMessage("Bạn có chắc chắn muốn đặt đơn hàng này không?")
                .setPositiveButton("Xác nhận", (dialog, which) -> performCheckout())
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav == null) return;

        // Đồng bộ Menu theo quyền
        bottomNav.getMenu().clear();
        if ("admin".equals(userRole)) {
            bottomNav.inflateMenu(R.menu.bottom_nav_admin_menu);
        } else {
            bottomNav.inflateMenu(R.menu.bottom_nav_menu);
        }

        // Đánh dấu tab "Giỏ hàng" nếu có trong menu
        MenuItem cartItem = bottomNav.getMenu().findItem(R.id.nav_cart);
        if (cartItem != null) {
//            cartItem.setChecked(true);
            bottomNav.setSelectedItemId(R.id.nav_cart);
        } else {
            // Nếu là Admin (không có tab giỏ hàng), bỏ chọn tất cả để tránh nhầm lẫn
            bottomNav.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < bottomNav.getMenu().size(); i++) {
                bottomNav.getMenu().getItem(i).setChecked(false);
            }
            bottomNav.getMenu().setGroupCheckable(0, true, true);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            
            // Nếu đang ở đúng tab này rồi thì không làm gì
            if (id == R.id.nav_cart) return true;

            Intent intent = null;
            if (id == R.id.nav_home || id == R.id.nav_books) {
                intent = new Intent(this, "admin".equals(userRole) ? AdminActivity.class : MainActivity.class);
            } else if (id == R.id.nav_orders) {
                intent = new Intent(this, "admin".equals(userRole) ? OrdersActivity.class : UserOrdersActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            } else if (id == R.id.nav_users) {
                intent = new Intent(this, UsersManagementActivity.class);
            }

            if (intent != null) {
                intent.putExtra("USERNAME", username);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void performCheckout() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getBookPrice() * item.getQuantity();
        }

        OrderDAO orderDAO = new OrderDAO(this);
        long orderId = orderDAO.placeOrder(userId, total, cartItems);

        if (orderId != -1) {
            cartDAO.clearCart(userId);
            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
            
            // Chuyển sang màn hình Đơn hàng phù hợp
            Intent intent = new Intent(this, "admin".equals(userRole) ? OrdersActivity.class : UserOrdersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Có lỗi xảy ra khi đặt hàng", Toast.LENGTH_SHORT).show();
        }
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
                loadCartData(); 
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
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                total += item.getBookPrice() * item.getQuantity();
            }
        }
        tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", total));
    }
}