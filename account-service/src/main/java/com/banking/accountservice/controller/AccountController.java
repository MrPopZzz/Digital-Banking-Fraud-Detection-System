package com.banking.accountservice.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

	private final AccountService accountService;
	
	@PostMapping
	public ResponseEntity<AccountResponse> createAccount(
			@Valid @RequestBody CreateAccountRequest request) 
	{
		return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
	}
	
	@GetMapping("/{accountNumber}")
	public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
		return ResponseEntity.ok(accountService.getAccount(accountNumber));
	}
	
	@GetMapping("/{accountNumber}/balance")
	public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
		return ResponseEntity.ok(accountService.getBalance(accountNumber));
	}
	
	@PutMapping("/{accountNumber}/block")
	public ResponseEntity<String> blockAccount(@PathVariable String accountNumber) {
		accountService.blockAccount(accountNumber);
		return ResponseEntity.ok("ACCOUNT BLOCKED SUCCESSFULLY");
	}
	
	/**
	 * SAGA Step 1 - Deduct balance
	 * called by Transaction Service when transfer is initiated
	 */
	
	@PutMapping("/{accountNumber}/deduct")
	public ResponseEntity<String> deductBalance(
			@PathVariable String accountNumber, @RequestParam BigDecimal amount)
	{
		accountService.deductBalance(accountNumber, amount);
		return ResponseEntity.ok("BALANCE DEDUCTED SUCCESSFULLY");
		
	}
	
	/**
	 * SAGA Step 4 - Compensating Transaction endpoint
	 * called by Transaction Service in TWO SCENARIOS
	 *  1. Fraud detected - Refund sender (undo step 1)
	 *  2. Transaction completed - Credit reciever
	 */
	
	@PutMapping("/{accountNumber}/credit")
	public ResponseEntity<String> creditBalance(@PathVariable String accountNumber, 
			@RequestParam BigDecimal amount)
	{
		accountService.creditBalance(accountNumber, amount);
		return ResponseEntity.ok("BALANCE CREDITED SUCCESSFULLY");
	}
}
