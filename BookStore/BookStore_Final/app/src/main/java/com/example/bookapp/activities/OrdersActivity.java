package com.example.bookapp.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private List<Order> fullOrderList = new ArrayList<>();
    private OrderDAO orderDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // KIỂM TRA QUYỀN TRUY CẬP: Chỉ cho phép admin vào màn hình này
        String role = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("ROLE", "user");
        if (!"admin".equals(role)) {
            // Nếu không phải admin, chuyển hướng ngay sang màn hình đơn hàng của User
            startActivity(new Intent(this, UserOrdersActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_orders);

        orderDAO = new OrderDAO(this);
        initViews();
        setupRecyclerView();
        loadOrders();
        setupSearch();
        setupNavigation();
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvOrders);
    }

    private void setupRecyclerView() {
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        // isAdmin = true -> Hiện nút Duyệt/Từ chối cho Admin
        adapter = new OrderAdapter(this, orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onApprove(Order order) {
                updateOrderStatus(order, "Accepted");
            }

            @Override
            public void onReject(Order order) {
                updateOrderStatus(order, "Rejected");
            }
            @Override
            public void onOrderClick(Order order) {
                showOrderDetailDialog(order);
            }
        }, true);
        rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        orderList.clear();
        fullOrderList.clear();
        orderList.addAll(orderDAO.getAllOrders()); // Admin lấy tất cả đơn hàng
        fullOrderList.addAll(orderList);
        adapter.notifyDataSetChanged();
    }

    private void showOrderDetailDialog(Order order) {
        try {
            Order fullOrder = orderDAO.getOrderById(order.getId());
            List<OrderItem> orderItems = orderDAO.getOrderItems(order.getId());
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_order_detail);

            TextView tvOrderId = dialog.findViewById(R.id.tvOrderId);
            TextView tvCustomerName = dialog.findViewById(R.id.tvCustomerName);
            TextView tvOrderDate = dialog.findViewById(R.id.tvOrderDate);
            TextView tvTotalAmount = dialog.findViewById(R.id.tvTotalAmount);
            RecyclerView rvOrderItems = dialog.findViewById(R.id.rvOrderItems);
            Button btnClose = dialog.findViewById(R.id.btnClose);

            tvOrderId.setText("Mã đơn: #" + fullOrder.getId());
            tvCustomerName.setText("Khách hàng: " + fullOrder.getUserName());
            tvOrderDate.setText("Ngày đặt: " + fullOrder.getOrderDate());
            tvTotalAmount.setText(String.format("Tổng tiền: %,d đ", (int) fullOrder.getTotalAmount()));

            rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
            rvOrderItems.setAdapter(new OrderItemAdapter(this, orderItems));

            btnClose.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi hiển thị chi tiết", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterOrders(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterOrders(newText);
                return false;
            }
        });
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(R.menu.bottom_nav_admin_menu);
        bottomNav.setSelectedItemId(R.id.nav_orders);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_books) {
                startActivity(new Intent(this, AdminActivity.class));
                return true;
            } else if (itemId == R.id.nav_users) {
                startActivity(new Intent(this, UsersManagementActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void filterOrders(String query) {
        orderList.clear();
        if (query.isEmpty()) {
            orderList.addAll(fullOrderList);
        } else {
            for (Order order : fullOrderList) {
                if (String.valueOf(order.getId()).contains(query) ||
                        order.getUserName().toLowerCase().contains(query.toLowerCase())) {
                    orderList.add(order);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateOrderStatus(Order order, String newStatus) {
        if (orderDAO.updateOrderStatus(order.getId(), newStatus)) {
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            loadOrders();
        }
    }
}