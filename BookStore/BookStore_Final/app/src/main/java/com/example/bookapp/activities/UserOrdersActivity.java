package com.example.bookapp.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.R;
import com.example.bookapp.adapters.OrderAdapter;
import com.example.bookapp.adapters.OrderItemAdapter;
import com.example.bookapp.database.OrderDAO;
import com.example.bookapp.models.Order;
import com.example.bookapp.models.OrderItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class UserOrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private OrderDAO orderDAO;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_orders);

        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("USER_ID", -1);
        orderDAO = new OrderDAO(this);

        initViews();
        setupRecyclerView();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi màn hình quay trở lại (từ Giỏ hàng hoặc Tài khoản)
        loadOrders();
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvOrders);
    }

    private void setupRecyclerView() {
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        // isAdmin = false -> Không hiện nút Duyệt/Từ chối
        adapter = new OrderAdapter(this, orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onApprove(Order order) {} // Không dùng
            @Override
            public void onReject(Order order) {}  // Không dùng
            @Override
            public void onOrderClick(Order order) {
                showOrderDetailDialog(order);
            }
        }, false);
        rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        if (userId == -1) return;
        
        orderList.clear();
        List<Order> latestOrders = orderDAO.getOrdersByUserId(userId);
        if (latestOrders != null) {
            orderList.addAll(latestOrders);
        }
        adapter.notifyDataSetChanged();
    }

    private void showOrderDetailDialog(Order order) {
        try {
            Order fullOrder = orderDAO.getOrderById(order.getId());
            List<OrderItem> orderItems = orderDAO.getOrderItems(order.getId());

            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_order_detail);

            TextView tvOrderId = dialog.findViewById(R.id.tvOrderId);
            TextView tvOrderDate = dialog.findViewById(R.id.tvOrderDate);
            TextView tvOrderStatus = dialog.findViewById(R.id.tvOrderStatus);
            TextView tvTotalAmount = dialog.findViewById(R.id.tvTotalAmount);
            RecyclerView rvOrderItems = dialog.findViewById(R.id.rvOrderItems);
            Button btnClose = dialog.findViewById(R.id.btnClose);

            tvOrderId.setText("Mã đơn: #" + fullOrder.getId());
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
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_cart) {
                Intent intent = new Intent(this, CartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}