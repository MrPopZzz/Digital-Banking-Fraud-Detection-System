package com.banking.accountservice.service;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

	private final AccountRepository accountRepository;
	private static SecureRandom secureRandom = new SecureRandom();
	
	/**
	 * Create new Account
	 * @param request
	 * @return
	 */
	public AccountResponse createAccount(CreateAccountRequest request) {
		log.info("Creating account for: {}", request.getEmail());
		
		if(accountRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Account already exists for email: " + request.getEmail());
		}
		
		Account account = Account.builder()
				.accountHolderName(request.getAccountHolderName())
				.email(request.getEmail())
				.phone(request.getPhone())
				.accountType(request.getAccountType())
				.accountStatus(AccountStatus.ACTIVE)
				.balance(request.getInitialDeposit())
				.accountNumber(generateAccountNumber())
				.dailyTransactionLimit(request.getAccountType() == AccountType.SAVINGS
						? new BigDecimal("100000")
						: new BigDecimal("500000"))
				.build();
		
		Account savedAccount = accountRepository.save(account);
		log.info("Account created successfully: {}", savedAccount.getAccountNumber());
		
		return mapToResponse(savedAccount);
	}
	
	/**
	 * Get Account by account number
	 * @param accountNumber
	 * @return
	 */
	public AccountResponse getAccount(String accountNumber) {
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new RuntimeException("Account not found"));
		
		return mapToResponse(account);
	}
	
	/**
	 * Get Balance by account number
	 * @param accountNumber
	 * @return
	 */
	public BigDecimal getBalance(String accountNumber) {
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new RuntimeException("Account not found"));
		
		return account.getBalance();
	}
	
	/**
	 * Block Account  - called by Fraud Detection Service via kafka
	 * @param accountNumber
	 */
	public void blockAccount(String accountNumber) {
		log.info("Blocking account: {}", accountNumber);
		
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new RuntimeException("Account not found"));
		account.setAccountStatus(AccountStatus.BLOCKED);
		accountRepository.save(account);
		
		log.info("Account blocked: {}", accountNumber);	
	}
	
	/**
	 * Deduct Account from Sender Account
	 * called by Transaction Service via kafka
	 * @param accountNumber
	 * @param amount
	 */
	public void deductBalance(String accountNumber, BigDecimal amount) {
		log.info("Deducting balance {} from account: {}", amount, accountNumber);
		
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new RuntimeException("Account not found"));
		
		if(account.getAccountStatus() != AccountStatus.ACTIVE) {
			throw new RuntimeException("Account is not active - " + accountNumber);
		}
		
		if(account.getBalance().compareTo(amount) < 0) {
			throw new RuntimeException("Insufficient funds for account: " + accountNumber);
		}
		
		account.setBalance(account.getBalance().subtract(amount));
		accountRepository.save(account);
		
		log.info("Balance updated. New balance: {}", account.getBalance());
	}
	
	/**
	 * Credit Balance to Receiver Account
	 * called by Transaction Service via kafka
	 * @param accountNumber
	 * @param amount
	 */
	public void creditBalance(String accountNumber, BigDecimal amount) {
		log.info("Crediting {} to account: {}", amount, accountNumber);
		
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new RuntimeException("Account not found"));
		
		account.setBalance(account.getBalance().add(amount));
		accountRepository.save(account);
		
		log.info("Balance credited. New balance: {}", account.getBalance());
	}
	
	
	
	/**
	 * Helper methods
	 * @return
	 */
	
	// Generate unique 12 digit account number
	private String generateAccountNumber() {
		String accountNumber;
		
		do {
			long number = secureRandom.nextLong(1_000_000_000_000L);
			accountNumber = String.format("%012d", number);
		} while(accountRepository.existsByAccountNumber(accountNumber));
		
		return accountNumber;	
	}
	
	// Map from entity to dto
	private AccountResponse mapToResponse(Account account) {
		AccountResponse response = new AccountResponse();
		
		response.setId(account.getId());
		response.setAccountNumber(account.getAccountNumber());
		response.setAccountHolderName(account.getAccountHolderName());
		response.setEmail(account.getEmail());
		response.setPhone(account.getPhone());
		response.setAccountType(account.getAccountType());
		response.setAccountStatus(account.getAccountStatus());
		response.setBalance(account.getBalance());
		response.setDailyTransactionLimit(account.getDailyTransactionLimit());
		
		return response;
	}
}
