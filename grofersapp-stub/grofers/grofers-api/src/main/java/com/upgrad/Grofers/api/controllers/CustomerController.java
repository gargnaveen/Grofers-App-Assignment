package com.upgrad.Grofers.api.controllers;


import com.upgrad.Grofers.api.*;
import com.upgrad.Grofers.service.business.CustomerService;
import com.upgrad.Grofers.service.entity.CustomerAuthEntity;
import com.upgrad.Grofers.service.entity.CustomerEntity;
import com.upgrad.Grofers.service.exception.AuthenticationFailedException;
import com.upgrad.Grofers.service.exception.AuthorizationFailedException;
import com.upgrad.Grofers.service.exception.SignUpRestrictedException;
import com.upgrad.Grofers.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/customer")
public class CustomerController {
	@Autowired
	private CustomerService customerService;


	/**
	 * A controller method for customer signup.
	 *
	 * @param signupCustomerRequest - This argument contains all the attributes required to store customer details in the database.
	 * @return - ResponseEntity<SignupCustomerResponse> type object along with Http status CREATED.
	 * @throws SignUpRestrictedException
	 */

	@PostMapping("/signup")
	public ResponseEntity<SignupCustomerResponse> signup(@RequestBody SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException {
		CustomerEntity customerEntity = null;
		customerEntity = customerService.saveCustomer(new CustomerEntity(UUID.randomUUID().toString(), signupCustomerRequest.getFirstName(), signupCustomerRequest.getLastName(), signupCustomerRequest.getEmailAddress(), signupCustomerRequest.getContactNumber(), signupCustomerRequest.getPassword()));
		SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse();
		signupCustomerResponse.status("CUSTOMER SUCCESSFULLY REGISTERED");
		signupCustomerResponse.id(customerEntity.getUuid());
		ResponseEntity<SignupCustomerResponse> response = new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.OK);
		return response;
	}

	/**
	 * A controller method for customer authentication.
	 *
	 * @param authorization - A field in the request header which contains the customer credentials as Basic authentication.
	 * @return - ResponseEntity<LoginResponse> type object along with Http status OK.
	 * @throws AuthenticationFailedException
	 */
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> customerLogin(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {

	}

	/**
	 * A controller method for customer logout.
	 *
	 * @param authorization - A field in the request header which contains the JWT token.
	 * @return - ResponseEntity<LogoutResponse> type object along with Http status OK.
	 * @throws AuthorizationFailedException
	 */
	@PostMapping("/logout")
	public ResponseEntity<LogoutResponse> customerLogout(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
		CustomerAuthEntity customerAuthEntity = customerService.logout(authorization);
		CustomerEntity customerEntity = customerAuthEntity.getCustomer();
		LogoutResponse logoutResponse = new LogoutResponse().id(customerEntity.getUuid()).message("Logged Out Successfully!");
		return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
	}

	/**
	 * A controller method for updating customer password.
	 *
	 * @param updatePasswordRequest - This argument contains all the attributes required to update customer password in the database.
	 * @param authorization         - A field in the request header which contains the JWT token.
	 * @return - ResponseEntity<LogoutResponse> type object along with Http status OK.
	 * @throws AuthorizationFailedException
	 * @throws UpdateCustomerException
	 */
	@PutMapping("/Password")
	public ResponseEntity<LogoutResponse> updatePassword(@RequestHeader("authorization") String authorization, @RequestBody UpdatePasswordRequest updatePasswordRequest) throws AuthorizationFailedException, UpdateCustomerException {

		if(updatePasswordRequest.getNewPassword()==null||updatePasswordRequest.getOldPassword()==null)
			throw new UpdateCustomerException("UCR-003","No field should be empty.");

		customerService.authorization(authorization);
		CustomerEntity customerEntity = customerService.getCustomer(authorization);
		customerService.updateCustomerPassword(updatePasswordRequest.getOldPassword(),updatePasswordRequest.getNewPassword(),customerEntity);

		LogoutResponse logoutResponse = new LogoutResponse().id(customerEntity.getUuid()).message("CUSTOMER PASSWORD UPDATED SUCCESSFULLY!");
		return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
	}
}
