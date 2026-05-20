package com.example.bookapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.R;
import com.example.bookapp.adapters.CheckoutAdapter;
import com.example.bookapp.database.CartDAO;
import com.example.bookapp.database.OrderDAO;
import com.example.bookapp.database.UserDAO;
import com.example.bookapp.models.CartItem;
import com.example.bookapp.models.User;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private TextView tvUserName, tvUserPhone, tvUserAddress;
    private TextView tvSubtotal, tvShippingFee, tvTotalDetails, tvFinalTotal;
    private List<CartItem> selectedItems;
    private int userId;
    private double subtotal = 0;
    private final double shippingFee = 30000;
    private User currentUser;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("USER_ID", -1);
        userDAO = new UserDAO(this);

        selectedItems = (List<CartItem>) getIntent().getSerializableExtra("SELECTED_ITEMS");

        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào được chọn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserInfo();
        setupRecyclerView();
        calculatePrices();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvUserName = findViewById(R.id.tvUserName);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvUserAddress = findViewById(R.id.tvUserAddress);
        tvSubtotal = findViewById(R.id.tvSubtotalPrice);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotalDetails = findViewById(R.id.tvTotalPriceDetails);
        tvFinalTotal = findViewById(R.id.tvFinalTotal);
        rvProducts = findViewById(R.id.rvCheckoutProducts);

        findViewById(R.id.btnPlaceOrder).setOnClickListener(v -> checkInfoAndPlaceOrder());
        
        // Cho phép nhấn vào vùng địa chỉ để cập nhật
        findViewById(R.id.tvUserPhone).setOnClickListener(v -> showUpdateInfoDialog());
        findViewById(R.id.tvUserAddress).setOnClickListener(v -> showUpdateInfoDialog());
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = prefs.getString("USERNAME", "");
        currentUser = userDAO.getUserByUsername(username);

        if (currentUser != null) {
            tvUserName.setText(currentUser.getFullname() != null && !currentUser.getFullname().isEmpty() 
                    ? currentUser.getFullname() : currentUser.getUsername());
            
            updateAddressUI();
        }
    }

    private void updateAddressUI() {
        if (currentUser.getPhone() != null && !currentUser.getPhone().trim().isEmpty()) {
            tvUserPhone.setText(currentUser.getPhone());
            tvUserPhone.setTextColor(getResources().getColor(android.R.color.tab_indicator_text));
        } else {
            tvUserPhone.setText("Chưa có số điện thoại (Nhấp để thêm)");
            tvUserPhone.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        if (currentUser.getAddress() != null && !currentUser.getAddress().trim().isEmpty()) {
            tvUserAddress.setText(currentUser.getAddress());
            tvUserAddress.setTextColor(getResources().getColor(android.R.color.tab_indicator_text));
        } else {
            tvUserAddress.setText("Chưa có địa chỉ (Nhấp để thêm)");
            tvUserAddress.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        CheckoutAdapter adapter = new CheckoutAdapter(this, selectedItems);
        rvProducts.setAdapter(adapter);
    }

    private void calculatePrices() {
        subtotal = 0;
        for (CartItem item : selectedItems) {
            subtotal += item.getBookPrice() * item.getQuantity();
        }

        double total = subtotal + shippingFee;

        tvSubtotal.setText(String.format(Locale.getDefault(), "%,.0f đ", subtotal));
        tvShippingFee.setText(String.format(Locale.getDefault(), "%,.0f đ", shippingFee));
        tvTotalDetails.setText(String.format(Locale.getDefault(), "%,.0f đ", total));
        tvFinalTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", total));
    }

    private void checkInfoAndPlaceOrder() {
        if (currentUser == null) return;

        String phone = currentUser.getPhone();
        String address = currentUser.getAddress();

        if (phone == null || phone.trim().isEmpty() || address == null || address.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng cập nhật đầy đủ số điện thoại và địa chỉ", Toast.LENGTH_LONG).show();
            showUpdateInfoDialog();
        } else {
            placeOrder();
        }
    }

    private void showUpdateInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật thông tin giao hàng");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_info, null);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etAddress = view.findViewById(R.id.etAddress);

        if (currentUser.getPhone() != null) etPhone.setText(currentUser.getPhone());
        if (currentUser.getAddress() != null) etAddress.setText(currentUser.getAddress());

        builder.setView(view);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newPhone = etPhone.getText().toString().trim();
            String newAddress = etAddress.getText().toString().trim();

            if (newPhone.isEmpty() || newAddress.isEmpty()) {
                Toast.makeText(this, "Không được để trống thông tin", Toast.LENGTH_SHORT).show();
            } else {
                currentUser.setPhone(newPhone);
                currentUser.setAddress(newAddress);
                userDAO.updateUserInfo(currentUser);
                updateAddressUI();
                Toast.makeText(this, "Đã cập nhật thông tin", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void placeOrder() {
        OrderDAO orderDAO = new OrderDAO(this);
        CartDAO cartDAO = new CartDAO(this);
        
        long orderId = orderDAO.placeOrder(userId, subtotal + shippingFee, selectedItems);

        if (orderId != -1) {
            for (CartItem item : selectedItems) {
                cartDAO.removeFromCart(item.getId());
            }
            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
            
            Intent intent = new Intent(this, UserOrdersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Có lỗi xảy ra khi đặt hàng", Toast.LENGTH_SHORT).show();
        }
    }
}
