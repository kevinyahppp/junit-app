package com.junit.app.repositories;

import com.junit.app.models.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankRepository extends JpaRepository<Bank, Long> {
//    List<Bank> findAll();
//    Bank findById(Long id);
//    void save(Bank bank);
}
