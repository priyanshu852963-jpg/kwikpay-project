package com.kwikpay.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BalanceService {
    // Using AtomicLong for thread-safe operations without complex synchronization
    private final AtomicLong balance = new AtomicLong(0);

    @PostConstruct
    public void init() {
        // Set initial balance to 1,000,000 on every application start
        balance.set(1_000_000L);
    }

    public long getBalance() {
        return balance.get();
    }

    /**
     * Deposits a specified amount into the account.
     * @param amount The amount to add. Must be positive.
     */
    public void deposit(long amount) {
        if (amount > 0) {
            balance.addAndGet(amount);
        }
    }

    /**
     * Withdraws a specified amount from the account.
     * @param amount The amount to withdraw. Must be positive.
     * @return true if the withdrawal was successful, false if balance was insufficient.
     */
    public boolean withdraw(long amount) {
        if (amount <= 0) {
            return false; // Cannot withdraw zero or a negative amount
        }
        // Loop to ensure the update happens correctly in a multi-threaded environment
        while (true) {
            long currentBalance = balance.get();
            if (currentBalance < amount) {
                return false; // Insufficient funds
            }
            long nextBalance = currentBalance - amount;
            // Compare-and-set: atomically sets the value if the current value is as expected.
            // This prevents race conditions.
            if (balance.compareAndSet(currentBalance, nextBalance)) {
                return true; // Success
            }
        }
    }
}