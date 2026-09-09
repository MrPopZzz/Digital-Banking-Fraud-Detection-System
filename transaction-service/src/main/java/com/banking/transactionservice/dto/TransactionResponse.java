package com.banking.transactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banking.transactionservice.entity.TranactionType;
import com.banking.transactionservice.entity.TransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

	private String id;
	private String senderAccountNumber;
	private String receiverAccountNumber;
	private BigDecimal amount;
	private TranactionType type;
	private TransactionStatus status;
	private String description;
	private String failureReason;
	private String referenceNumber;
	private LocalDateTime createdAt;
	private LocalDateTime completedAt;
}
