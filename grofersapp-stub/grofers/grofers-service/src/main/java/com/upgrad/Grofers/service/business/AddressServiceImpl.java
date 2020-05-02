package com.upgrad.Grofers.service.business;

import com.upgrad.Grofers.service.dao.AddressDao;
import com.upgrad.Grofers.service.entity.AddressEntity;
import com.upgrad.Grofers.service.entity.CustomerAddressEntity;
import com.upgrad.Grofers.service.entity.CustomerEntity;
import com.upgrad.Grofers.service.entity.StateEntity;
import com.upgrad.Grofers.service.exception.AddressNotFoundException;
import com.upgrad.Grofers.service.exception.AuthorizationFailedException;
import com.upgrad.Grofers.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class AddressServiceImpl implements AddressService {

	@Autowired
	private AddressDao addressDao;

	/**
	 * The method implements the business logic for save address endpoint.
	 */
	@Override @Transactional(propagation = Propagation.REQUIRED) public AddressEntity saveAddress(AddressEntity addressEntity, CustomerAddressEntity customerAddressEntity) throws SaveAddressException {

		Pattern digitPattern = Pattern.compile("\\d{6}");
		if(!digitPattern.matcher(addressEntity.getPincode()).matches()) {
			throw new SaveAddressException("SGR-002", "Invalid Pincode!");
		}

		addressDao.saveAddress(addressEntity);
		saveCustomerAddress(customerAddressEntity);
		return addressEntity;


	}

	/**
	 * The method implements the business logic for get address by uuid endpoint.
	 */
	@Override public AddressEntity getAddressByUUID(String addressId, CustomerEntity customerEntity) throws AuthorizationFailedException, AddressNotFoundException {
	}


	/**
	 * The method implements the business logic for saving customer address.
	 */
	@Transactional(propagation = Propagation.REQUIRED) public CustomerAddressEntity saveCustomerAddress(CustomerAddressEntity customerAddressEntity) {
		addressDao.saveCustomerAddress(customerAddressEntity);
	}

	/**
	 * The method implements the business logic for delete address endpoint.
	 */
	@Override @Transactional(propagation = Propagation.REQUIRED) public AddressEntity deleteAddress(AddressEntity addressEntity) {
	}

	/**
	 * The method implements the business logic for getting all saved address endpoint.
	 */
	@Override public List<AddressEntity> getAllAddress(CustomerEntity customer) {
		List<AddressEntity> addressEntityList = addressDao.getAllAddress(customer);
		return addressEntityList;
	}

	/**
	 * The method implements the business logic for getting state by id.
	 */
	@Override public StateEntity getStateByUUID(String uuid) throws AddressNotFoundException {

		StateEntity stateEntity = addressDao.getStateByUUID(uuid);
		if(stateEntity==null)
			throw new AddressNotFoundException("ANF-002","No state by this id!");
		return stateEntity;
	}

}
