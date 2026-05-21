package com.example.bookapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookapp.R;
import com.example.bookapp.database.BookDAO;
import com.example.bookapp.database.CartDAO;
import com.example.bookapp.models.Book;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView ivBookImage;
    private TextView tvBookPrice, tvBookTitle, tvBookAuthor, tvBookCategory, tvBookStock, tvBookDescription;
    private ImageButton btnBack, btnShare, btnFavorite;
    private MaterialButton btnAddToCart, btnBuyNow;
    private int bookId;
    private Book currentBook;
    private BookDAO bookDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        bookId = getIntent().getIntExtra("BOOK_ID", -1);
        bookDAO = new BookDAO(this);
        currentBook = bookDAO.getBookById(bookId);

        if (currentBook == null) {
            Toast.makeText(this, "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displayBookDetails();
    }

    private void initViews() {
        ivBookImage = findViewById(R.id.ivBookImage);
        tvBookPrice = findViewById(R.id.tvBookPrice);
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvBookAuthor = findViewById(R.id.tvBookAuthor);
        tvBookCategory = findViewById(R.id.tvBookCategory);
        tvBookStock = findViewById(R.id.tvBookStock);
        tvBookDescription = findViewById(R.id.tvBookDescription);
        
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnFavorite = findViewById(R.id.btnFavorite);
        
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        btnBack.setOnClickListener(v -> finish());
        
        btnAddToCart.setOnClickListener(v -> addToCart());
        
        btnBuyNow.setOnClickListener(v -> {
            addToCart();
            startActivity(new Intent(this, CartActivity.class));
        });
        
        btnShare.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Xem cuốn sách này trên BookApp: " + currentBook.getTitle());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, null));
        });

        btnFavorite.setOnClickListener(v -> {
            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayBookDetails() {
        tvBookTitle.setText(currentBook.getTitle());
        tvBookAuthor.setText(currentBook.getAuthor());
        tvBookCategory.setText(currentBook.getCategory());
        tvBookDescription.setText(currentBook.getDescription());
        tvBookPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", currentBook.getPrice()));
        tvBookStock.setText("Còn hàng: " + currentBook.getStock() + " cuốn");

        Glide.with(this)
                .load(currentBook.getImage())
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivBookImage);
    }

    private void addToCart() {
        int userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        CartDAO cartDAO = new CartDAO(this);
        cartDAO.addToCart(userId, currentBook.getId(), 1);
        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }
}
