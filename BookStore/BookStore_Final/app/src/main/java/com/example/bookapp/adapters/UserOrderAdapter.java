package com.example.bookapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookapp.R;
import com.example.bookapp.models.Order;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Locale;

public class UserOrderAdapter extends RecyclerView.Adapter<UserOrderAdapter.UserOrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onCancelOrder(Order order);
        void onReorder(Order order);
        void onShowDetails(Order order);
    }

    public UserOrderAdapter(Context context, List<Order> orderList, OnOrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_order, parent, false);
        return new UserOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserOrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Đơn hàng #" + order.getId());
        holder.tvOrderDate.setText(order.getOrderDate());
        holder.tvTotalAmount.setText(String.format(Locale.getDefault(), "%,.0f đ", order.getTotalAmount()));
        
        String status = order.getStatus();
        holder.tvOrderStatus.setText(translateStatus(status));
        holder.tvOrderStatus.setTextColor(getStatusColor(status));

        // Logic hiển thị nút
        holder.btnCancel.setVisibility(View.GONE);
        holder.btnReorder.setVisibility(View.GONE);

        if ("Pending".equalsIgnoreCase(status)) {
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else if ("Cancelled".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
            holder.btnReorder.setVisibility(View.VISIBLE);
        }

        holder.btnCancel.setOnClickListener(v -> listener.onCancelOrder(order));
        holder.btnReorder.setOnClickListener(v -> listener.onReorder(order));
        holder.btnDetails.setOnClickListener(v -> listener.onShowDetails(order));
    }

    private String translateStatus(String status) {
        switch (status) {
            case "Pending": return "Chờ xác nhận";
            case "Accepted": return "Đã xác nhận";
            case "Shipped": return "Đang giao";
            case "Completed": return "Hoàn thành";
            case "Cancelled":
            case "Rejected": return "Đã hủy";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "Pending": return Color.parseColor("#FF9800"); // Orange
            case "Accepted": return Color.parseColor("#4CAF50"); // Green
            case "Shipped": return Color.parseColor("#2196F3"); // Blue
            case "Completed": return Color.parseColor("#2196F3"); // Blue
            case "Cancelled":
            case "Rejected": return Color.parseColor("#F44336"); // Red
            default: return Color.GRAY;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class UserOrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvOrderDate, tvTotalAmount;
        MaterialButton btnCancel, btnReorder, btnDetails;

        public UserOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnReorder = itemView.findViewById(R.id.btnReorder);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}
