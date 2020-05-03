package com.upgrad.Grofers.service.business;


import com.upgrad.Grofers.service.dao.CustomerDao;
import com.upgrad.Grofers.service.entity.CustomerAuthEntity;
import com.upgrad.Grofers.service.entity.CustomerEntity;
import com.upgrad.Grofers.service.exception.AuthenticationFailedException;
import com.upgrad.Grofers.service.exception.AuthorizationFailedException;
import com.upgrad.Grofers.service.exception.SignUpRestrictedException;
import com.upgrad.Grofers.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    /**
     * The method implements the business logic for saving customer details endpoint.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity saveCustomer(CustomerEntity customerEntity) throws SignUpRestrictedException {
        String regex = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(customerEntity.getEmail());
        if(customerDao.getCustomerByContactNumber(customerEntity.getContactNumber())!=null)
        {
            throw new SignUpRestrictedException("SGR-001","This contact number is already registered! Try other contact number.");
        }

        if(customerEntity.getContactNumber()==null||customerEntity.getEmail()==null||customerEntity.getFirstName()==null||customerEntity.getPassword()==null||customerEntity.getUuid()==null)
        {
            throw new SignUpRestrictedException("SGR-005","Except last name all fields should be filled");
        }

        if(!matcher.matches())
        {
            throw new SignUpRestrictedException("SGR-002","Invalid email-id format!");
        }

        if(!customerEntity.getContactNumber().matches("[0-9]{10}"))
        {
            throw new SignUpRestrictedException("SGR-003","Invalid contact number!");
        }

        String passwordRegrex = "^(?=.*\\d)(?=.*[A-Z]).{8,255}$";
        if(!customerEntity.getPassword().matches(passwordRegrex))
        {
            throw new SignUpRestrictedException("SGR-004","Weak password!");
        }

        String passwords[] = passwordCryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setPassword(passwords[1]);
        customerEntity.setSalt(passwords[0]);

        customerDao.saveCustomer(customerEntity);
        return customerEntity;
    }

    /**
     * The method implements the business logic for signin endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(String contactNumber, String password) throws AuthenticationFailedException {

        CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(contactNumber);
        if(customerEntity==null)
            throw new AuthenticationFailedException("ATH-001","This contact number has not been registered!");

        final String encryptedPassword = passwordCryptographyProvider.encrypt(password, customerEntity.getSalt());
        if(encryptedPassword.equals(customerEntity.getPassword())){
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
            customerAuthEntity.setUuid(customerEntity.getUuid());
            customerAuthEntity.setCustomer(customerEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(customerAuthEntity.getUuid(), now, expiresAt));
            customerAuthEntity.setLoginAt(now);
            customerAuthEntity.setExpiresAt(expiresAt);
            customerAuthEntity.setUuid(customerAuthEntity.getUuid());

            customerDao.createCustomerAuth(customerAuthEntity);

            return customerAuthEntity;
        }
        else{
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials!");
        }
    }


    /**
     * The method implements the business logic for logout endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity logout(String access_token) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthByAccesstoken(access_token);
        authorization(access_token);
        customerAuthEntity.setLogoutAt(ZonedDateTime.now());
        return customerDao.updateCustomerAuth(customerAuthEntity);
    }

    /**
     * The method implements the business logic for updating customer password endpoint.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomerPassword(String oldPassword,String newPassword, CustomerEntity customerEntity) throws UpdateCustomerException {
        final String encryptedOldPassword = PasswordCryptographyProvider.encrypt(oldPassword, customerEntity.getSalt());

        String passwordRegrex = "^(?=.*\\d)(?=.*[A-Z]).{8,255}$";
        if(!newPassword.matches(passwordRegrex))
        {
            throw new UpdateCustomerException("UCR-001","Weak password!");
        }

        if(!customerEntity.getPassword().equals(encryptedOldPassword))
        {
            throw new UpdateCustomerException("UCR-004","Incorrect old password!");
        }

        String passwords[] = passwordCryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setPassword(passwords[1]);
        customerEntity.setSalt(passwords[0]);
        customerDao.updateCustomer(customerEntity);

        return customerEntity;

    }


    /**
     * The method implements the business logic for checking authorization of any customer.
     */
    @Override
    public void authorization(String access_token) throws AuthorizationFailedException {

        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthByAccesstoken(access_token);


        if(customerAuthEntity==null)
            throw new AuthorizationFailedException("ATHR-001","Customer is not logged in!");

        if(customerAuthEntity.getLogoutAt()!=null)
            throw new AuthorizationFailedException("ATHR-002","Customer is logged out. Log in again to access this end point");

        final ZonedDateTime now = ZonedDateTime.now();
        if(customerAuthEntity.getExpiresAt().isBefore(now))
            throw new AuthorizationFailedException("AUTHR-003","Your session is expired. Please log in again to access this end point");

    }

    /**
     * The method implements the business logic for getting customer details by access token.
     */
    @Override
    public CustomerEntity getCustomer(String access_token) throws AuthorizationFailedException {

        authorization(access_token);
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthByAccesstoken(access_token);
        return customerAuthEntity.getCustomer();
    }

}
