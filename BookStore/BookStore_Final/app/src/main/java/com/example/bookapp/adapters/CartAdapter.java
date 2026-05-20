package com.example.bookapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bookapp.R;
import com.example.bookapp.models.CartItem;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChange(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
        void onSelectionChange();
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemChangeListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.tvTitle.setText(item.getBookTitle());
        holder.tvPrice.setText(String.format("%,.0f đ", item.getBookPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.cbSelected.setChecked(item.isSelected());

        Glide.with(context)
                .load(item.getBookImage())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivBook);

        holder.btnPlus.setOnClickListener(v -> listener.onQuantityChange(item, item.getQuantity() + 1));
        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                listener.onQuantityChange(item, item.getQuantity() - 1);
            }
        });
        holder.btnRemove.setOnClickListener(v -> listener.onRemoveItem(item));
        
        holder.cbSelected.setOnClickListener(v -> {
            item.setSelected(holder.cbSelected.isChecked());
            listener.onSelectionChange();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBook;
        TextView tvTitle, tvPrice, tvQuantity;
        ImageButton btnPlus, btnMinus, btnRemove;
        CheckBox cbSelected;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBook = itemView.findViewById(R.id.ivBookImage);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvPrice = itemView.findViewById(R.id.tvBookPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            cbSelected = itemView.findViewById(R.id.cbSelectedItem);
        }
    }
}
