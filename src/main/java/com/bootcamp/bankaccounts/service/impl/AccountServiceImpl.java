package com.bootcamp.bankaccounts.service.impl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bootcamp.bankaccounts.clients.CustomerRestClient;
import com.bootcamp.bankaccounts.clients.TransactionsRestClient;
import com.bootcamp.bankaccounts.dto.AccountRequestDto;
import com.bootcamp.bankaccounts.dto.AccountResponseDto;
import com.bootcamp.bankaccounts.dto.Message;
import com.bootcamp.bankaccounts.dto.Transaction;
import com.bootcamp.bankaccounts.dto.TypeAccountDto;
import com.bootcamp.bankaccounts.entity.Account;
import com.bootcamp.bankaccounts.repository.AccountRepository;
import com.bootcamp.bankaccounts.service.AccountService;
import com.bootcamp.bankaccounts.util.TypeAccount;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class AccountServiceImpl implements AccountService{
	
	private final TypeAccount typeAccount = new TypeAccount();

	@Autowired
    private AccountRepository accountRepository;
	
	@Autowired
    CustomerRestClient customerRestClient;
	
	@Autowired
	TransactionsRestClient transactionRestClient;
	
	@Override
	public Flux<Account> getAll() {
		return accountRepository.findAll();
	}

	@Override
	public Mono<Account> getAccountById(String accountId) {
		return accountRepository.findById(accountId);
	}

	@Override
	public Mono<AccountResponseDto> createAccountPerson(AccountRequestDto accountRequestDto) {
		TypeAccountDto newAccount = getTypeAccount(accountRequestDto.getTypeAccount());
		Account account = new Account(null,accountRequestDto.getCustomerId(),accountRequestDto.getTypeAccount(),newAccount.getType(), 0.00
				, newAccount.getMaintenance(), newAccount.getTransactions(), newAccount.getDayOperation()
				, accountRequestDto.getDateAccount(), accountRequestDto.getNumberAccount(), accountRequestDto.getTypeCustomer());
		return customerRestClient.getPersonById(accountRequestDto.getCustomerId()).flatMap(c ->{
			account.setTypeCustomer(c.getTypeCustomer());
			return getAccountByIdCustomerPerson(accountRequestDto.getCustomerId(),newAccount.getType()).flatMap(v -> {
				return Mono.just(new AccountResponseDto(null, "Personal client already has a bank account: "+newAccount.getType()));
			}).switchIfEmpty(saveNewAccount(account));
		}).defaultIfEmpty(new AccountResponseDto(null, "Client does not exist"));
	}
	
	@Override
	public Mono<AccountResponseDto> createAccountCompany(AccountRequestDto accountRequestDto) {
		TypeAccountDto newAccount = getTypeAccount(accountRequestDto.getTypeAccount());
		Account account = new Account(null,accountRequestDto.getCustomerId(),accountRequestDto.getTypeAccount(),newAccount.getType(), 0.00
				, newAccount.getMaintenance(), newAccount.getTransactions(), newAccount.getDayOperation()
				, accountRequestDto.getDateAccount(), accountRequestDto.getNumberAccount(), accountRequestDto.getTypeCustomer());	
		return customerRestClient.getCompanyById(accountRequestDto.getCustomerId()).flatMap(c ->{
			account.setTypeCustomer(c.getTypeCustomer());
			if(newAccount.getType().equals("C_CORRIENTE")) {
				return saveNewAccount(account);
			}
			return Mono.just(new AccountResponseDto(null, "For company only type of account: C_CORRIENTE"));
		}).defaultIfEmpty(new AccountResponseDto(null, "Client does not exist"));
	}

	@Override
	public Mono<Account> updateAccount(AccountRequestDto accountRequestDto) {
		return accountRepository.findById(accountRequestDto.getId())
                .flatMap(uAccount -> {
                	uAccount.setCustomerId(accountRequestDto.getCustomerId());
                	uAccount.setTypeAccount(accountRequestDto.getTypeAccount());
                	uAccount.setDescripTypeAccount(getTypeAccount(accountRequestDto.getTypeAccount()).getType());
                	uAccount.setAmount(accountRequestDto.getAmount());
                	uAccount.setMaintenance(accountRequestDto.getMaintenance());
                	uAccount.setTransaction(accountRequestDto.getTransaction());
                	uAccount.setOperationDay(accountRequestDto.getOperationDay());
                	uAccount.setDateAccount(accountRequestDto.getDateAccount());
                	uAccount.setNumberAccount(accountRequestDto.getNumberAccount());
                	uAccount.setTypeCustomer(accountRequestDto.getTypeCustomer());
                    return accountRepository.save(uAccount);
        });
	}

	@Override
	public Mono<Message> deleteAccount(String accountId) {
		Message message = new Message("Account does not exist");
		return accountRepository.findById(accountId)
                .flatMap(dAccount -> {
                	message.setMessage("Account deleted successfully");
                	return accountRepository.deleteById(dAccount.getId()).thenReturn(message);
        }).defaultIfEmpty(message);
	}
	
	@Override
	public Mono<AccountResponseDto> depositAccount(AccountRequestDto accountRequestDto) {
		LocalDateTime myDateObj = LocalDateTime.now();
		return accountRepository.findById(accountRequestDto.getId()).filter(a -> a.getTransaction()>=0).flatMap(uAccount -> {
			if(uAccount.getTransaction() - 1 >= 0) {
				if(uAccount.getDescripTypeAccount().equals("PLAZO_FIJO") && uAccount.getOperationDay()!=myDateObj.getDayOfMonth()) {
					return Mono.just(new AccountResponseDto(null, "Day of the month not allowed for PLAZO_FIJO"));
				}
				uAccount.setAmount(uAccount.getAmount() + accountRequestDto.getAmount());
				uAccount.setTransaction(uAccount.getTransaction() - 1);
	            return accountRepository.save(uAccount).flatMap(account -> {
	            	return registerTransaction(uAccount, accountRequestDto.getAmount(),"DEPOSITO");
	            });
			}
			return Mono.just(new AccountResponseDto(null, "Exhausted monthly movements limit"));
        });
	}

	@Override
	public Mono<AccountResponseDto> withdrawalAccount(AccountRequestDto accountRequestDto) {
		LocalDateTime myDateObj = LocalDateTime.now();
		return accountRepository.findById(accountRequestDto.getId()).flatMap(uAccount -> {
			if(uAccount.getTransaction() - 1 >= 0) {
				Double amount = uAccount.getAmount() - accountRequestDto.getAmount();
				if(amount >= 0) {
					if(uAccount.getDescripTypeAccount().equals("PLAZO_FIJO") && uAccount.getOperationDay()!=myDateObj.getDayOfMonth()) {
						return Mono.just(new AccountResponseDto(null, "Day of the month not allowed for PLAZO_FIJO"));
					}
					uAccount.setAmount(amount);
					uAccount.setTransaction(uAccount.getTransaction() - 1);
		            return accountRepository.save(uAccount).flatMap(account -> {
		            	return registerTransaction(uAccount, accountRequestDto.getAmount(),"RETIRO");
		            });
				}
				return Mono.just(new AccountResponseDto(null, "You don't have enough balance"));
			}
			return Mono.just(new AccountResponseDto(null, "Exhausted monthly movements limit"));
        });
	}

	@Override
	public Flux<Account> getAllAccountXCustomerId(String customerId) {
		return accountRepository.findAll()
				.filter(c -> c.getCustomerId().equals(customerId));
	}

	@Override
	public Mono<Message> restartTransactions() {
		return updateTransaction().collectList().flatMap(c -> {
			return Mono.just(new Message("The number of transactions of the accounts was satisfactorily restarted"));
		});
	}
	
	private TypeAccountDto getTypeAccount(Integer idType) {
		Predicate<TypeAccountDto> p = f -> f.getId()==idType;
		TypeAccountDto type = typeAccount.getAccounts().filter(p).collect(Collectors.toList()).get(0);
		return type;
    }

	private Mono<Account> getAccountByIdCustomerPerson(String customerId, String type) {
		Flux<Account> r = accountRepository.findAll()
				.filter(c -> c.getCustomerId().equals(customerId))
				.filter(c -> c.getDescripTypeAccount().equals(type));
		Mono<Account> m= r.next();
		return m;
	}
	
	private Mono<AccountResponseDto> saveNewAccount(Account account) {
		return accountRepository.save(account).flatMap(x -> {
			return Mono.just(new AccountResponseDto(account, "Account created successfully"));
		});
	}
	
	private Mono<AccountResponseDto> registerTransaction(Account uAccount, Double amount, String typeTransaction){
		Transaction transaction = new Transaction();
		transaction.setCustomerId(uAccount.getCustomerId());
		transaction.setProductId(uAccount.getId());
		transaction.setProductType(uAccount.getDescripTypeAccount());
		transaction.setTransactionType(typeTransaction);
		transaction.setAmount(amount);
		transaction.setTransactionDate(new Date());
		transaction.setCustomerType(uAccount.getTypeCustomer());
		return transactionRestClient.createTransaction(transaction).flatMap(t -> {
			return Mono.just(new AccountResponseDto(uAccount, "Successful transaction"));
        });
	}
	
	private Flux<Account> updateTransaction(){
		return  accountRepository.findAll()
				.flatMap(c -> {
					c.setTransaction(getTypeAccount(c.getTypeAccount()).getTransactions());
					return accountRepository.save(c);
				});
	}
	
}
