package com.kwikpay.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;        // SEND, RECHARGE, ELECTRICITY
    private String target;      // UPI ID, mobile number, or biller
    private long amount;        // in paise or integer rupees (we'll use rupees for demo)
    private String status;      // SUCCESS / FAILED / PENDING
    private LocalDateTime createdAt;

    public Transaction() {}

    public Transaction(String type, String target, long amount, String status) {
        this.type = type;
        this.target = target;
        this.amount = amount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // getters and setters
    // (for brevity you can generate them in your IDE or use Lombok @Data)
    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}
    public String getType(){return type;}
    public void setType(String type){this.type=type;}
    public String getTarget(){return target;}
    public void setTarget(String target){this.target=target;}
    public long getAmount(){return amount;}
    public void setAmount(long amount){this.amount=amount;}
    public String getStatus(){return status;}
    public void setStatus(String status){this.status=status;}
    public java.time.LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(java.time.LocalDateTime createdAt){this.createdAt=createdAt;}
}
