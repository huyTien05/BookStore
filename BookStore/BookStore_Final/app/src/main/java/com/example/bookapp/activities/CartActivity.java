package com.example.bookapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.R;
import com.example.bookapp.adapters.CartAdapter;
import com.example.bookapp.database.CartDAO;
import com.example.bookapp.models.CartItem;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvTotalPrice, tvItemCount, tvDeleteSelected;
    private CheckBox cbSelectAll;
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

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("USER_ID", -1);
        userRole = prefs.getString("ROLE", "user");
        username = prefs.getString("USERNAME", null);

        cartDAO = new CartDAO(this);
        initViews();
        setupRecyclerView();
        loadCartData();
    }

    private void initViews() {
        rvCart = findViewById(R.id.rvCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvDeleteSelected = findViewById(R.id.tvDeleteSelected);
        cbSelectAll = findViewById(R.id.cbSelectAll);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        cbSelectAll.setOnClickListener(v -> {
            boolean isChecked = cbSelectAll.isChecked();
            for (CartItem item : cartItems) {
                item.setSelected(isChecked);
            }
            adapter.notifyDataSetChanged();
            updateTotalPrice();
        });

        tvDeleteSelected.setOnClickListener(v -> deleteSelectedItems());

        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            List<CartItem> selectedItems = getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putExtra("SELECTED_ITEMS", (Serializable) selectedItems);
                startActivity(intent);
            }
        });
    }

    private List<CartItem> getSelectedItems() {
        List<CartItem> selected = new ArrayList<>();
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                if (item.isSelected()) {
                    selected.add(item);
                }
            }
        }
        return selected;
    }

    private void deleteSelectedItems() {
        List<CartItem> selected = getSelectedItems();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Chưa chọn sản phẩm nào để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa các sản phẩm đã chọn?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    for (CartItem item : selected) {
                        cartDAO.removeFromCart(item.getId());
                    }
                    loadCartData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupRecyclerView() {
        rvCart.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadCartData() {
        cartItems = cartDAO.getCartByUser(userId);
        if (cartItems.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
            findViewById(R.id.layoutSelectAll).setVisibility(View.GONE);
            findViewById(R.id.layoutCheckout).setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvCart.setVisibility(View.VISIBLE);
            findViewById(R.id.layoutSelectAll).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutCheckout).setVisibility(View.VISIBLE);
        }

        adapter = new CartAdapter(this, cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChange(CartItem item, int newQuantity) {
                cartDAO.updateQuantity(item.getId(), newQuantity);
                item.setQuantity(newQuantity);
                adapter.notifyDataSetChanged();
                updateTotalPrice();
            }

            @Override
            public void onRemoveItem(CartItem item) {
                cartDAO.removeFromCart(item.getId());
                loadCartData();
            }

            @Override
            public void onSelectionChange() {
                updateSelectAllState();
                updateTotalPrice();
            }
        });
        rvCart.setAdapter(adapter);
        updateTotalPrice();
        updateSelectAllState();
    }

    private void updateSelectAllState() {
        if (cartItems == null || cartItems.isEmpty()) {
            cbSelectAll.setChecked(false);
            return;
        }
        boolean allSelected = true;
        for (CartItem item : cartItems) {
            if (!item.isSelected()) {
                allSelected = false;
                break;
            }
        }
        cbSelectAll.setChecked(allSelected);
    }

    private void updateTotalPrice() {
        double total = 0;
        int count = 0;
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                if (item.isSelected()) {
                    total += item.getBookPrice() * item.getQuantity();
                    count++;
                }
            }
        }
        tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", total));
        tvItemCount.setText(String.format(Locale.getDefault(), "Tổng cộng (%d sản phẩm):", count));
    }
}
