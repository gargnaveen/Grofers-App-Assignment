package com.upgrad.Grofers.api.controllers;

import com.upgrad.Grofers.api.*;
import com.upgrad.Grofers.service.business.AddressService;
import com.upgrad.Grofers.service.business.CustomerService;
import com.upgrad.Grofers.service.entity.AddressEntity;
import com.upgrad.Grofers.service.entity.CustomerAddressEntity;
import com.upgrad.Grofers.service.entity.CustomerEntity;
import com.upgrad.Grofers.service.exception.AddressNotFoundException;
import com.upgrad.Grofers.service.exception.AuthorizationFailedException;
import com.upgrad.Grofers.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("address")
public class AddressController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AddressService addressService;

    /**
     * A controller method to save an address in the database.
     *
     * @body SaveAddressRequest - This argument contains all the attributes required to store address details in the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<SaveAddressResponse> type object along with Http status CREATED.
     * @throws AuthorizationFailedException
     * @throws SaveAddressException
     * @throws AddressNotFoundException
     */

    @PostMapping("/")
   public ResponseEntity<SaveAddressResponse> saveAddress(@RequestHeader("authorization") String authorization,@RequestBody SaveAddressRequest saveAddressRequest) throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {

        customerService.authorization(authorization);

        if(saveAddressRequest.getCity()==null||saveAddressRequest.getFlatBuildingName()==null||saveAddressRequest.getLocality()==null||saveAddressRequest.getPincode()==null||saveAddressRequest.getStateUuid()==null)
        {
            throw new SaveAddressException("SAR-001","No feild can be empty!");
        }

        AddressEntity addressEntity = new AddressEntity(saveAddressRequest.getStateUuid(),saveAddressRequest.getFlatBuildingName(),saveAddressRequest.getLocality(),saveAddressRequest.getCity(),saveAddressRequest.getPincode(),addressService.getStateByUUID(saveAddressRequest.getStateUuid()));

        CustomerEntity customerEntity = customerService.getCustomer(authorization);

        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setAddress(addressEntity);
        customerAddressEntity.setCustomer(customerEntity);
        addressService.saveAddress(addressEntity,customerAddressEntity);

        SaveAddressResponse saveAddressResponse = new SaveAddressResponse().id(addressEntity.getUuid()).status("Address successfully registered.");

        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse, HttpStatus.OK);
    }



    /**
     * A controller method to delete an address from the database.
     *
     * @param addressId    - The uuid of the address to be deleted from the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<DeleteAddressResponse> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws AddressNotFoundException
     */
    @DeleteMapping("/{address_id}")
    public ResponseEntity<DeleteAddressResponse> deleteAddresses(@RequestHeader("authorization") String authorization, @RequestParam("addressId") String addressId) throws AddressNotFoundException, AuthorizationFailedException
    {
        customerService.authorization(authorization);
        if(addressId==null)
            throw new AddressNotFoundException("ANF-005","AddressId cannot be empty!");

        CustomerEntity customerEntity = customerService.getCustomer(authorization);
        AddressEntity addressEntity = addressService.getAddressByUUID(addressId,customerEntity);
        AddressEntity addressEntityDeleted = addressService.deleteAddress(addressEntity);
        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse().id(UUID.fromString(addressEntityDeleted.getUuid())).status("Address Deleted Successfully!");
        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);
    }



    /**
     * A controller method to get all address from the database.
     *
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<AddressListResponse> type object along with Http status OK.
     * @throws AuthorizationFailedException
     */
    @GetMapping("/customer")
    public ResponseEntity<AddressListResponse> getAddresses(@RequestHeader("authorization") String authorization) throws AuthorizationFailedException {
        {
            customerService.authorization(authorization);
            CustomerEntity customerEntity = customerService.getCustomer(authorization);
            List<AddressEntity> addressEntityList = addressService.getAllAddress(customerEntity);

            AddressList addressList = new AddressList();
            List<AddressList> addressListList = null;

            AddressListResponse addressListResponse = new AddressListResponse();
            addressListResponse.setAddresses(addressListList);

            return new ResponseEntity<AddressListResponse>(addressListResponse, HttpStatus.OK);
        }


    }
}
