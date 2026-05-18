package com.example.bookapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.R;
import com.example.bookapp.models.Order;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderActionListener listener;
    private boolean isAdmin; // Thêm biến để phân biệt quyền

    public interface OnOrderActionListener {
        void onApprove(Order order);
        void onReject(Order order);
        void onOrderClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList, OnOrderActionListener listener, boolean isAdmin) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Mã đơn: #" + order.getId());
        holder.tvUserName.setText("Người đặt: " + order.getUserName());
        holder.tvOrderDate.setText("Ngày đặt: " + order.getOrderDate());
        holder.tvTotalAmount.setText(String.format("%,d đ", (int) order.getTotalAmount()));

        updateStatusChip(holder.chipStatus, order.getStatus());

        // Chỉ hiển thị nút hành động nếu là Admin và đơn hàng đang ở trạng thái Pending
        if (isAdmin && "Pending".equals(order.getStatus())) {
            holder.llActionButtons.setVisibility(View.VISIBLE);
        } else {
            holder.llActionButtons.setVisibility(View.GONE);
        }

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(order));
        holder.btnReject.setOnClickListener(v -> showRejectConfirmation(order));

        holder.itemView.setOnClickListener(v -> listener.onOrderClick(order));
    }

    private void showRejectConfirmation(Order order) {
        new android.app.AlertDialog.Builder(context)
                .setTitle("Xác nhận từ chối")
                .setMessage("Bạn có chắc chắn muốn từ chối đơn hàng #" + order.getId() + "?")
                .setPositiveButton("Từ chối", (dialog, which) -> {
                    if (listener != null) {
                        listener.onReject(order);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateStatusChip(Chip chip, String status) {
        chip.setText(status);
        switch (status) {
            case "Pending": chip.setChipBackgroundColorResource(R.color.status_pending); break;
            case "Accepted": chip.setChipBackgroundColorResource(R.color.status_accepted); break;
            case "Rejected": chip.setChipBackgroundColorResource(R.color.status_rejected); break;
            case "Completed": chip.setChipBackgroundColorResource(R.color.status_completed); break;
            case "Shipped": chip.setChipBackgroundColorResource(R.color.status_shipped); break;
            default: chip.setChipBackgroundColorResource(R.color.status_default); break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvUserName, tvOrderDate, tvTotalAmount;
        Chip chipStatus;
        MaterialButton btnApprove, btnReject;
        LinearLayout llActionButtons;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            llActionButtons = itemView.findViewById(R.id.llActionButtons);
        }
    }
}