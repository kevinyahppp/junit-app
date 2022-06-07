package com.junit.app.models;

import com.junit.app.exceptions.NotEnoughMoney;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal balance;

    public void debit(BigDecimal amount) {
        BigDecimal newBalance = balance.subtract(amount);
        if(newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughMoney("Not enough money for the account");
        }
        this.balance = newBalance;
    }

    public void credit(BigDecimal amount) {
        this.balance = balance.add(amount);
    }
}
