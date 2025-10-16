package com.kwikpay.controller;

import com.kwikpay.model.Transaction;
import com.kwikpay.repository.TransactionRepository;
import com.kwikpay.service.BalanceService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TransactionController {

    // Demo password (change as needed)
    private final String APP_PASSWORD = "kwikpay123";

    private final BalanceService balanceService;
    private final TransactionRepository txRepo;

    public TransactionController(BalanceService balanceService, TransactionRepository txRepo) {
        this.balanceService = balanceService;
        this.txRepo = txRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String password = body.getOrDefault("password", "");
        if (APP_PASSWORD.equals(password)) {
            return ResponseEntity.ok(Map.of("ok", true, "message", "Authenticated"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false, "message", "Wrong password"));
        }
    }

    @GetMapping("/balance")
    public Map<String, Object> getBalance() {
        return Map.of("balance", balanceService.getBalance());
    }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody Map<String, String> body) {
        // expected body: { password, type (SEND/RECHARGE/ELECTRICITY), target, amount }
        String password = body.getOrDefault("password", "");
        if (!APP_PASSWORD.equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false, "message", "Wrong password"));
        }

        String type = body.getOrDefault("type", "SEND");
        String target = body.getOrDefault("target", "unknown");
        long amount;
        try { amount = Long.parseLong(body.getOrDefault("amount","0")); }
        catch (NumberFormatException e) { amount = 0; }

        if (amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Invalid amount"));
        }

        boolean ok = balanceService.withdraw(amount);
        Transaction tx;
        if (ok) {
            tx = new Transaction(type, target, amount, "SUCCESS");
            txRepo.save(tx);
            return ResponseEntity.ok(Map.of("ok", true, "message", "Transaction successful", "balance", balanceService.getBalance()));
        } else {
            tx = new Transaction(type, target, amount, "FAILED");
            txRepo.save(tx);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("ok", false, "message", "Insufficient balance"));
        }
    }

    @GetMapping("/transactions")
    public List<Transaction> getTxs() {
        return txRepo.findAll();
    }

    @PostMapping("/topup")
    public ResponseEntity<?> topup(@RequestBody Map<String, String> body) {
        // for demo: allow adding money (not required but helpful during testing)
        String password = body.getOrDefault("password", "");
        if (!APP_PASSWORD.equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false, "message", "Wrong password"));
        }
        long amount = Long.parseLong(body.getOrDefault("amount","0"));
        if (amount <= 0) return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Invalid amount"));
        balanceService.deposit(amount);
        return ResponseEntity.ok(Map.of("ok", true, "balance", balanceService.getBalance()));
    }
}
