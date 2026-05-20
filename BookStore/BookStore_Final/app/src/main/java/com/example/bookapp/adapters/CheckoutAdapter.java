package com.example.bookapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bookapp.R;
import com.example.bookapp.models.CartItem;
import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private Context context;
    private List<CartItem> items;

    public CheckoutAdapter(Context context, List<CartItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.tvTitle.setText(item.getBookTitle());
        holder.tvPrice.setText(String.format("%,.0f đ", item.getBookPrice()));
        holder.tvQuantity.setText("x" + item.getQuantity());
        holder.tvSubtotal.setText(String.format("%,.0f đ", item.getBookPrice() * item.getQuantity()));

        Glide.with(context)
                .load(item.getBookImage())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivBook);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBook;
        TextView tvTitle, tvPrice, tvQuantity, tvSubtotal;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBook = itemView.findViewById(R.id.ivBookImage);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvPrice = itemView.findViewById(R.id.tvBookPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvItemSubtotal);
        }
    }
}
