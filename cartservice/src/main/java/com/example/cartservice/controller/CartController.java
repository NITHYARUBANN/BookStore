package com.example.cartservice.controller;

import com.example.cartservice.entity.Cart;
import com.example.cartservice.entity.PurchaseOrder;
import com.example.cartservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carts")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("/getAll")
    public ResponseEntity<List<Cart>> getAllCarts() {
        return ResponseEntity.ok(cartService.getAllCarts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCartById(@PathVariable int id) {
        Cart cart = cartService.getCartById(id);
        if (cart == null) {
            return new ResponseEntity<>("No Cart is found with id " + id, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addBookToCart(@RequestParam int bookId, @RequestParam int quantity) {
        Cart cart = cartService.addBookToCart(bookId, quantity);
        if (cart == null) {
            return new ResponseEntity<>("Book not found or insufficient quantity in bookservice", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable int id) {
        cartService.deleteCart(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<PurchaseOrder> checkout(@RequestParam Long userId, @RequestParam String username, @RequestParam String address) {
        PurchaseOrder purchaseOrder = cartService.checkout(userId, username, address);
        return ResponseEntity.ok(purchaseOrder);
    }

    @GetMapping("/orderHistory/{userId}")
    public ResponseEntity<List<PurchaseOrder>> getOrderHistory(@PathVariable Long userId) {
        List<PurchaseOrder> orders = cartService.getOrderHistory(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/allOrders")
    public ResponseEntity<List<PurchaseOrder>> getAllOrders() {
        List<PurchaseOrder> orders = cartService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    @GetMapping("/total")
    public ResponseEntity<Double> getTotalPrice() {
        double totalValue = cartService.calculateTotalValue();
        return ResponseEntity.ok(totalValue);
    }
}