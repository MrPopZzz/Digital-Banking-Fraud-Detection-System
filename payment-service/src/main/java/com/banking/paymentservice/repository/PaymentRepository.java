package com.banking.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banking.paymentservice.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {

}
