package com.banking.transactionservice.entity;

/**
 * Transaction Lifecycle Flow:
 * 
 * 1. PENDING -> PROCESSING -> COMPLETED (clean transaction)
 * 2.                       -> PENDING_VERIFICATION (suspicious activity detected)
 *                                       -> COMPLETED (verified)
 *                                       -> FLAGGED (SAGA REFUND)
 *                          -> FAILED
 *                          -> FLAGGED
 */
public enum TransactionStatus {
	PENDING,
	PROCESSING,
	COMPLETED,
	PENDING_VERIFICATION,
	FAILED,
	FLAGGED
}
