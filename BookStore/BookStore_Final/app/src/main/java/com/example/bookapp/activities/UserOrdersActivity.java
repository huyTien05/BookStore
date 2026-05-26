package com.example.bookapp.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.R;
import com.example.bookapp.adapters.OrderItemAdapter;
import com.example.bookapp.adapters.UserOrderAdapter;
import com.example.bookapp.database.CartDAO;
import com.example.bookapp.database.OrderDAO;
import com.example.bookapp.models.Order;
import com.example.bookapp.models.OrderItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class UserOrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private UserOrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> displayedOrders = new ArrayList<>();
    private OrderDAO orderDAO;
    private int userId;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_orders);

        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("USER_ID", -1);
        orderDAO = new OrderDAO(this);

        initViews();
        setupRecyclerView();
        setupTabLayout();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.nav_orders).setChecked(true);
        }
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvOrders);
        tabLayout = findViewById(R.id.tabLayout);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserOrderAdapter(this, displayedOrders, new UserOrderAdapter.OnOrderActionListener() {
            @Override
            public void onCancelOrder(Order order) {
                showCancelConfirmation(order);
            }

            @Override
            public void onReorder(Order order) {
                performReorder(order);
            }

            @Override
            public void onShowDetails(Order order) {
                showOrderDetailDialog(order);
            }
        });
        rvOrders.setAdapter(adapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterOrders(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadOrders() {
        if (userId == -1) return;
        
        allOrders.clear();
        List<Order> latestOrders = orderDAO.getOrdersByUserId(userId);
        if (latestOrders != null) {
            allOrders.addAll(latestOrders);
        }
        filterOrders(tabLayout.getSelectedTabPosition());
    }

    private void filterOrders(int tabPosition) {
        displayedOrders.clear();
        String filterStatus = "";
        switch (tabPosition) {
            case 1: filterStatus = "Pending"; break;
            case 2: filterStatus = "Accepted"; break;
            case 3: filterStatus = "Shipped"; break;
            case 4: filterStatus = "Completed"; break;
            case 5: filterStatus = "Cancelled"; break;
        }

        if (tabPosition == 0) {
            displayedOrders.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                if (tabPosition == 5) {
                    if ("Cancelled".equalsIgnoreCase(order.getStatus()) || "Rejected".equalsIgnoreCase(order.getStatus())) {
                        displayedOrders.add(order);
                    }
                } else if (filterStatus.equalsIgnoreCase(order.getStatus())) {
                    displayedOrders.add(order);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showCancelConfirmation(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #" + order.getId() + "?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    if (orderDAO.updateOrderStatus(order.getId(), "Cancelled")) {
                        Toast.makeText(this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                        loadOrders();
                    }
                })
                .setNegativeButton("Quay lại", null)
                .show();
    }

    private void performReorder(Order order) {
        List<OrderItem> items = orderDAO.getOrderItems(order.getId());
        CartDAO cartDAO = new CartDAO(this);
        for (OrderItem item : items) {
            cartDAO.addToCart(userId, item.getBookId(), item.getQuantity());
        }
        Toast.makeText(this, "Đã thêm các sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
    }

    private void showOrderDetailDialog(Order order) {
        try {
            Order fullOrder = orderDAO.getOrderById(order.getId());
            List<OrderItem> orderItems = orderDAO.getOrderItems(order.getId());

            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_order_detail);

            TextView tvOrderId = dialog.findViewById(R.id.tvOrderId);
            TextView tvCustomerName = dialog.findViewById(R.id.tvCustomerName);
            TextView tvCustomerPhone = dialog.findViewById(R.id.tvCustomerPhone);
            TextView tvCustomerAddress = dialog.findViewById(R.id.tvCustomerAddress);
            TextView tvOrderDate = dialog.findViewById(R.id.tvOrderDate);
            TextView tvOrderStatus = dialog.findViewById(R.id.tvOrderStatus);
            TextView tvTotalAmount = dialog.findViewById(R.id.tvTotalAmount);
            RecyclerView rvOrderItems = dialog.findViewById(R.id.rvOrderItems);
            Button btnClose = dialog.findViewById(R.id.btnClose);
            Button btnShare = dialog.findViewById(R.id.btnShareInvoice);
            btnShare.setVisibility(View.GONE);

            tvOrderId.setText("Mã đơn: #" + fullOrder.getId());
            tvCustomerName.setText("Khách hàng: " + fullOrder.getUserName());
            tvCustomerPhone.setText("SĐT: " + (fullOrder.getUserPhone() != null ? fullOrder.getUserPhone() : "N/A"));
            tvCustomerAddress.setText("Địa chỉ: " + (fullOrder.getUserAddress() != null ? fullOrder.getUserAddress() : "N/A"));
            tvOrderDate.setText("Ngày đặt: " + fullOrder.getOrderDate());
            tvOrderStatus.setText("Trạng thái: " + fullOrder.getStatus());
            tvTotalAmount.setText(String.format("Tổng tiền: %,d đ", (int) fullOrder.getTotalAmount()));

            rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
            rvOrderItems.setAdapter(new OrderItemAdapter(this, orderItems));

            btnClose.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xem chi tiết", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_orders);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_orders) {
                return true;
            } else if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return false;
            } else if (itemId == R.id.nav_cart) {
                Intent intent = new Intent(this, CartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return false;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return false;
            }
            return false;
        });
    }
}
