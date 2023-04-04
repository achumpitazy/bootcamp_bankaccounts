package com.bootcamp.bankaccounts.controller;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.bootcamp.bankaccounts.dto.AccountRequestDto;
import com.bootcamp.bankaccounts.dto.AccountResponseDto;
import com.bootcamp.bankaccounts.dto.Message;
import com.bootcamp.bankaccounts.entity.Account;
import com.bootcamp.bankaccounts.service.AccountService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@WebFluxTest (AccountController.class)
public class AccountControllerTest {
	
	@MockBean
    private AccountService accountService;

    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    public void testGetAll() {
        Flux<Account> accountFlux = Flux.just(new Account("0123450001","00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 3, 12, 0, 0), "04112122", "PERSON", 6.00), new Account("0123450002","00002",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON", 6.00));
        
        when(accountService.getAll()).thenReturn(accountFlux);
        Flux<Account> responseBody = webTestClient.get()
                .uri("/account")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Account.class)
                .getResponseBody();
        
        StepVerifier.create(responseBody)
        .expectSubscription()
        .expectNext(new Account("0123450001","00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 3, 12, 0, 0), "04112122", "PERSON", 6.00))
        .expectNext(new Account("0123450002","00002",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON", 6.00))
        .verifyComplete();
    }
	
	
	@Test
    public void testGetAccountById() {
        Account account = new Account("0123456789","00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.now(), "04112122", "PERSON", 6.00);
        Mono<Account> accountMono = Mono.just(new Account("0123456789","00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.now(), "04112122", "PERSON", 6.00));
        
        
        when(accountService.getAccountById(account.getId())).thenReturn(accountMono);
        webTestClient.get()
        .uri("/account/" + account.getId())
        .accept(MediaType.APPLICATION_NDJSON)
        .exchange()
        .expectStatus().isOk()
        .returnResult(Account.class)
        .getResponseBody();

		StepVerifier.create(accountMono)
		        .expectNext(account)
		        .verifyComplete();
    }
	
	@Test
    public void testCreateAccountPerson() {
		AccountRequestDto accountRequestDto = new AccountRequestDto(null,"00001",1,"C_CORRIENTE", 100.0, 100.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON");
        Account account = new Account(null,"00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON", 6.00);
        AccountResponseDto accountResponseDto = new AccountResponseDto("Account created successfully", account);
        Mono<AccountResponseDto> accountResponseDtoMono = Mono.just(new AccountResponseDto("Account created successfully", new Account(null,"00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON", 6.00)));       
        
        when(accountService.createAccountPerson(accountRequestDto)).thenReturn(accountResponseDtoMono);
        webTestClient.post()
        .uri("/account/person")
        .accept(MediaType.APPLICATION_NDJSON)
        .body(accountResponseDtoMono, AccountResponseDto.class)
        .exchange()
        .expectStatus().isOk();

		StepVerifier.create(accountResponseDtoMono)
		        .expectNext(accountResponseDto)
		        .verifyComplete();
    }
	
	@Test
    public void createAccountCompany() {
		AccountRequestDto accountRequestDto = new AccountRequestDto(null,"00001",1,"C_CORRIENTE", 100.0, 100.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "COMPANY");
        Account account = new Account(null,"00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "COMPANY", 6.00);
        AccountResponseDto accountResponseDto = new AccountResponseDto("Account created successfully", account);
        Mono<AccountResponseDto> accountResponseDtoMono = Mono.just(new AccountResponseDto("Account created successfully", new Account(null,"00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "COMPANY", 6.00)));       
        
        when(accountService.createAccountCompany(accountRequestDto)).thenReturn(accountResponseDtoMono);
        webTestClient.post()
        .uri("/account/company")
        .accept(MediaType.APPLICATION_NDJSON)
        .body(accountResponseDtoMono, AccountResponseDto.class)
        .exchange()
        .expectStatus().isOk();

		StepVerifier.create(accountResponseDtoMono)
		        .expectNext(accountResponseDto)
		        .verifyComplete();
    }
	
	@Test
    public void TestDepositAccount() {
		AccountRequestDto accountRequestDto = new AccountRequestDto(null,"00001",1,"C_CORRIENTE", 100.0, 100.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON");
        Account account = new Account(null,"00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON", 6.00);
        AccountResponseDto accountResponseDto = new AccountResponseDto("Successful transaction", account);
        Mono<AccountResponseDto> accountResponseDtoMono = Mono.just(new AccountResponseDto("Successful transaction", new Account(null,"00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON", 6.00)));       
        
        when(accountService.depositAccount(accountRequestDto)).thenReturn(accountResponseDtoMono);
        webTestClient.post()
        .uri("/account/deposit")
        .accept(MediaType.APPLICATION_NDJSON)
        .body(accountResponseDtoMono, AccountResponseDto.class)
        .exchange()
        .expectStatus().isOk();

		StepVerifier.create(accountResponseDtoMono)
		        .expectNext(accountResponseDto)
		        .verifyComplete();
    }
	
	@Test
    public void TestWithdrawalAccount() {
		AccountRequestDto accountRequestDto = new AccountRequestDto(null,"00001",1,"C_CORRIENTE", 100.0, 100.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON");
        Account account = new Account(null,"00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON", 6.00);
        AccountResponseDto accountResponseDto = new AccountResponseDto("Successful transaction", account);
        Mono<AccountResponseDto> accountResponseDtoMono = Mono.just(new AccountResponseDto("Successful transaction", new Account(null,"00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON", 6.00)));       
        
        when(accountService.withdrawalAccount(accountRequestDto)).thenReturn(accountResponseDtoMono);
        webTestClient.post()
        .uri("/account/deposit")
        .accept(MediaType.APPLICATION_NDJSON)
        .body(accountResponseDtoMono, AccountResponseDto.class)
        .exchange()
        .expectStatus().isOk();

		StepVerifier.create(accountResponseDtoMono)
		        .expectNext(accountResponseDto)
		        .verifyComplete();
    }
	
	@Test
	public void TestDeleteAccount(){
		Message message = new Message("Account deleted successfully");
        given(accountService.deleteAccount(any())).willReturn(Mono.just(message));
        webTestClient.delete()
                .uri("/account/5454sdfsdf545")
                .exchange()
                .expectStatus().isOk();
    }
	
	@Test
    public void testGetAllAccountXCustomerId() {
		Flux<Account> accountFlux = Flux.just(new Account("0123450001","00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 3, 12, 0, 0), "04112122", "PERSON", 6.00), new Account("0123450002","00001",1,"AHORRO", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON", 6.00));
        String id = "00001";
        when(accountService.getAllAccountXCustomerId(id)).thenReturn(accountFlux);
        Flux<Account> responseBody = webTestClient.get()
                .uri("/account/consult/"+id)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Account.class)
                .getResponseBody();
        
        StepVerifier.create(responseBody)
        .expectSubscription()
        .expectNext(new Account("0123450001","00001",1,"C_CORRIENTE", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 3, 12, 0, 0), "04112122", "PERSON", 6.00))
        .expectNext(new Account("0123450002","00001",1,"AHORRO", 100.0, 100.0, 5.0, 10, 15
				, LocalDateTime.of(2023, 4, 4, 12, 0, 0), "04112122", "PERSON", 6.00))
        .verifyComplete();
    }
	
	@Test
	public void TestRestartTransactions(){
		Message message = new Message("The number of transactions of the accounts was satisfactorily restarted");
        given(accountService.restartTransactions()).willReturn(Mono.just(message));
        webTestClient.delete()
                .uri("/account/restartTransactions")
                .exchange()
                .expectStatus().isOk();
    }

}
