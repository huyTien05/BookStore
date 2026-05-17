package com.example.bookapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.view.View;
import android.widget.TextView;

import com.example.bookapp.R;
import com.example.bookapp.adapters.BookAdapter;
import com.example.bookapp.database.BookDAO;
import com.example.bookapp.database.CartDAO;
import com.example.bookapp.models.Book;
import com.example.bookapp.utils.AiHelper;
import com.example.bookapp.utils.GridSpacingItemDecoration;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvBooks;
    private BookAdapter adapter;
    private List<Book> bookList = new ArrayList<>();
    private List<Book> fullBookList = new ArrayList<>();
    private BookDAO bookDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chuyển hướng nếu là Admin
        String role = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("ROLE", "user");
        if ("admin".equals(role)) {
            Intent intent = new Intent(this, AdminActivity.class);
            intent.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bookDAO = new BookDAO(this);
        rvBooks = findViewById(R.id.rvBooks);
        rvBooks.setLayoutManager(new GridLayoutManager(this, 2));

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        rvBooks.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        setupAdapter();
        loadBooks();

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, SearchResultsActivity.class);
                    intent.putExtra("SEARCH_QUERY", query);
                    startActivity(intent);
                }
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        String username = getIntent().getStringExtra("USERNAME");
        if (username != null) {
            Toast.makeText(this, "Chào mừng: " + username, Toast.LENGTH_SHORT).show();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    loadBooks();
                    return true;
                } else if (itemId == R.id.nav_cart) {
                    Intent intent = new Intent(MainActivity.this, CartActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_orders) {
                    // Chuyển hướng sang màn hình Đơn hàng của User
                    Intent intent = new Intent(MainActivity.this, UserOrdersActivity.class);
                    intent.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    intent.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupAdapter() {
        adapter = new BookAdapter(this, bookList, new BookAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                showAiDialog(book);
            }

            @Override
            public void onAddToCartClick(Book book) {
                addToCart(book);
            }
        });
        rvBooks.setAdapter(adapter);
    }

    private void addToCart(Book book) {
        int userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        CartDAO cartDAO = new CartDAO(this);
        cartDAO.addToCart(userId, book.getId(), 1);
        Toast.makeText(this, "Đã thêm \"" + book.getTitle() + "\" vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    private void loadBooks() {
        bookList.clear();
        fullBookList.clear();
        bookList.addAll(bookDAO.getAllBooks());
        fullBookList.addAll(bookList);
        adapter.notifyDataSetChanged();
    }

    private void showAiDialog(Book book) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_book_ai);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView tvBookName  = dialog.findViewById(R.id.tvBookName);
        View layoutLoading   = dialog.findViewById(R.id.layoutLoading);
        View scrollResult    = dialog.findViewById(R.id.scrollResult);
        TextView tvAiResult  = dialog.findViewById(R.id.tvAiResult);
        TextView tvError     = dialog.findViewById(R.id.tvError);
        View btnClose        = dialog.findViewById(R.id.btnClose);
        View btnAddToCartAi  = dialog.findViewById(R.id.btnAddToCartAi);

        tvBookName.setText("\"" + book.getTitle() + "\"");
        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnAddToCartAi.setOnClickListener(v -> {
            addToCart(book);
            dialog.dismiss();
        });

        String prompt = "Bạn là trợ lý tư vấn sách. Hãy trả lời NGẮN GỌN bằng tiếng Việt (tối đa 120 từ) về cuốn sách sau:\n\n"
                + "Tên: " + book.getTitle() + "\n"
                + "Tác giả: " + book.getAuthor() + "\n"
                + "Thể loại: " + book.getCategory() + "\n"
                + "Mô tả: " + book.getDescription() + "\n"
                + "Giá: " + String.format("%,.0f đ", book.getPrice()) + "\n\n"
                + "Hãy cho biết: 1) Sách này phù hợp với ai? 2) Có nên mua không? Dùng emoji cho sinh động.";

        AiHelper.ask(prompt, new AiHelper.Callback() {
            @Override
            public void onResult(String result) {
                layoutLoading.setVisibility(View.GONE);
                scrollResult.setVisibility(View.VISIBLE);
                tvAiResult.setText(result);
            }

            @Override
            public void onError(String errorMsg) {
                layoutLoading.setVisibility(View.GONE);
                tvError.setVisibility(View.VISIBLE);
            }
        });

        dialog.show();
    }
}