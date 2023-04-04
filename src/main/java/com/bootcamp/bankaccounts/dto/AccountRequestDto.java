package com.bootcamp.bankaccounts.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountRequestDto {
	private String id;
	private String customerId;
	@Min(value = 0,message = "0:AHORRO, 1:C_CORRIENTE, 2:PLAZO_FIJO")
	@Max(value = 2,message = "0:AHORRO, 1:C_CORRIENTE, 2:PLAZO_FIJO")
	private Integer typeAccount;
	private String descripTypeAccount;
	private Double amount;
	private Double maintenance;
	private Integer transaction;
	private Integer operationDay;
	private LocalDateTime dateAccount;
	private String NumberAccount;
	private String typeCustomer;
}
