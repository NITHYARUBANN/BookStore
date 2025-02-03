package com.example.cartservice.service;

import com.example.cartservice.entity.Book;
import com.example.cartservice.entity.Cart;
import com.example.cartservice.entity.PurchaseOrder;
import com.example.cartservice.repository.CartRepository;
import com.example.cartservice.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private RestTemplate restTemplate;

    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    public Cart getCartById(int id) {
        return cartRepository.findById(id).orElse(null);
    }

    public Cart addBookToCart(int bookId, int quantity) {
        // Check if the book exists and is available in bookservice
        String bookServiceUrl = "http://localhost:8002/books/" + bookId;
        ResponseEntity<Book> response = restTemplate.getForEntity(bookServiceUrl, Book.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            Book book = response.getBody();
            if (book != null && book.getQuantity() >= quantity) {
                // Update the book quantity in bookservice
                String updateBookUrl = "http://localhost:8002/books/edit/" + bookId;
                book.setQuantity(book.getQuantity() - quantity);
                restTemplate.put(updateBookUrl, book);

                // Add the book to the cart
                Cart cart = new Cart();
                cart.setBookId(bookId);
                cart.setName(book.getTitle()); // Set the book name
                cart.setQuantity(quantity);
                return cartRepository.save(cart);
            }
        }
        return null;
    }

    public void deleteCart(int id) {
        cartRepository.deleteById(id);
    }

    public double calculateTotalValue() {
        List<Cart> carts = cartRepository.findAll();
        double totalValue = 0.0;
        for (Cart cart : carts) {
            String bookServiceUrl = "http://localhost:8002/books/" + cart.getBookId();
            ResponseEntity<Book> response = restTemplate.getForEntity(bookServiceUrl, Book.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Book book = response.getBody();
                if (book != null) {
                    totalValue += book.getPrice() * cart.getQuantity();
                }
            }
        }
        return totalValue;
    }

    public PurchaseOrder checkout(Long userId, String username, String address) {
        List<Cart> carts = cartRepository.findAll();
        double totalValue = calculateTotalValue();
        StringBuilder orderDetails = new StringBuilder();

        for (Cart cart : carts) {
            orderDetails.append(cart.getName()).append(" (").append(cart.getQuantity()).append("), ");
        }

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setUserId(userId);
        purchaseOrder.setUsername(username);
        purchaseOrder.setOrderDetails(orderDetails.toString());
        purchaseOrder.setTotalCost(totalValue);
        purchaseOrder.setAddress(address);

        // Save the order
        purchaseOrderRepository.save(purchaseOrder);

        // Clear the cart
        cartRepository.deleteAll();

        return purchaseOrder;
    }

    public List<PurchaseOrder> getOrderHistory(Long userId) {
        return purchaseOrderRepository.findByUserId(userId);
    }

    public List<PurchaseOrder> getAllOrders() {
        return purchaseOrderRepository.findAll();
    }
}