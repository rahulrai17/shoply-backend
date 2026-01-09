# Let's discuss how we are going to manage our addresses

- We will create a set of apis that will allow users to manage their addresses that will be used for shipping Products to the customer.

Apis we will be creating :
1. Create Address
    - **Endpoint** : /addresses
    - **Method** : POST
    - **Purpose** : Create new Address
    - **Request Body** : AddressDTO
    - **Request Parameter** : None
    - **Response** : AddressDTO with HttpStatus.CREATED

2. Get All Addresses
    - **Endpoint** : /addresses
    - **Method** : GET
    - **Purpose** : Retrieve all address 
    - **Request Body** : None
    - **Request Parameter** : None
    - **Response** : List of AddressDTO with Http.status.OK

3. Get Address by ID
    - **Endpoint** : /addresses/{addressID}
    - **Method** : GET
    - **Purpose** : Retrieve aN address by its ID
    - **Request Body** : None
    - **Request Parameter** : Path: addressId(Long)
    - **Response** : AddressDTO with Http.status.OK

4. Get Address by User
    - **Endpoint** : /users/addresses
    - **Method** : GET
    - **Purpose** : Retrieve the logged-in user's addresses
    - **Request Body** : None
    - **Request Parameter** : None
    - **Response** : AddressDTO with Http.status.OK

5. Update Address 
    - **Endpoint** : /addresses/{addressID}
    - **Method** : PUT
    - **Purpose** : Update an existing address by its ID
    - **Request Body** : Address
    - **Request Parameter** : Path: addressId(Long)
    - **Response** : AddressDTO with Http.status.OK

6. Delete Address 
    - **Endpoint** : /addresses/{addressID}
    - **Method** : DELETE
    - **Purpose** : Delete an address by its ID
    - **Request Body** : None
    - **Request Parameter** : Path: addressId(Long)
    - **Response** : String message with Http.status.OK