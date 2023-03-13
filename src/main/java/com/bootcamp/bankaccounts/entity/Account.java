package com.bootcamp.bankaccounts.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="account")
public class Account {
	@Id
	private String id;
	@NotEmpty
	private String customerId;
	@NotEmpty
	@JsonIgnore
	private Integer typeAccount;
	private String descripTypeAccount;
	@NotEmpty
	private Double amount;
	@NotEmpty
	private Double maintenance;
	@NotEmpty
	private Integer transaction;
	@NotEmpty
	private Integer operationDay;
	@NotEmpty
	private Date dateAccount;
	@NotEmpty
	private String NumberAccount;
	
	private String typeCustomer;
	
}
