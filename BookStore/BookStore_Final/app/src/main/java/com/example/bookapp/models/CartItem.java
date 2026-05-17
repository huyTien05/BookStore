package com.example.bookapp.models;

public class CartItem {
    private int id;
    private int userId;
    private int bookId;
    private int quantity;
    private String bookTitle;
    private double bookPrice;
    private String bookImage;

    public CartItem(int id, int userId, int bookId, int quantity, String bookTitle, double bookPrice, String bookImage) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.bookTitle = bookTitle;
        this.bookPrice = bookPrice;
        this.bookImage = bookImage;
    }

    // Getters and Setters
    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getBookTitle() { return bookTitle; }
    public double getBookPrice() { return bookPrice; }
    public String getBookImage() { return bookImage; }
}
