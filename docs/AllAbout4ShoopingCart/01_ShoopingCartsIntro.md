# Shopping Cart in our Application

- There are three ways we can implement a cart.
  1. `Session-Based Carts` : 
     - Cart's content are stored in the user's session. If session expires, data is lost.
  2. `Cookie-Based Carts` : 
     - Cart data is stored in cookies on the user's browser.
  3. `Database-Based Carts `:
     - Cart data is stored on the server side, within a database. This approach is scalable, secure.
     - Also allows for advances features like cart recovery, detailed analytics, and cross-device accessibility.
     

- We will use `Database-Based Carts` :
    - Advantages of Database-Based Carts
        - Persistence and Reliability 
        - Scalability 
        - Enhanced Features (wishlist, personalised recommendation, etc)
        - Security
        - User Experience

## Designing Cart Module :

- We will have the usual layers :
    ```text
    Browser -> Controller -> service -> Repository -> Database
    ```
  
- Apis we will be creating :
1. Add product cart 
   - **Endpoint** : /api/carts/products/{productId}/quantity/{quantity}
   - **Method** : POST
   - **Purpose** : Adds a specific product and quantity to the user's cart
   - **Request Body** : None
   - **Request Parameter** : productId, quantity
   - **Response** : cartDTO(JSON)
   
2. Get All Carts
    - **Endpoint** : /api/carts
    - **Method** : GET
    - **Purpose** : Retrieves a list of all carts
    - **Request Body** : None
    - **Request Parameter** : None
    - **Response** : List of cartDTO(JSON)
   
3. Get User's Cart
    - **Endpoint** : /api/carts/users/cart
    - **Method** : GET
    - **Purpose** : Retrieves a list of the logged-in user
    - **Request Body** : None
    - **Request Parameter** : None
    - **Response** : cartDTO(JSON)
   
4. Update Product Quantity
    - **Endpoint** : /api/carts/products/{productId}/quantity/{operation}
    - **Method** : PUT
    - **Purpose** : Updates the quantity of a specific product in the cart
    - **Request Body** : None
    - **Request Parameter** : productId, operation
    - **Response** : cartDTO(JSON)
   
5. Delete Product from Cart
    - **Endpoint** : /api/carts/{cartId}/product/{productId}
    - **Method** : DELETE
    - **Purpose** : Removes a specific product from the user's cart.
    - **Request Body** : None
    - **Request Parameter** : cartId, productId
    - **Response** : String(Status Message)

- Now this will be authenticated apis since we need to add products to the logged-in user's cart.
- Therefore, session management is important.

## Now lets disuss the database models structure:

- We will have two models :
    1. cart :
       - cart_id
       - total_price
       - user_id
       
    2. cart_items :
       - cart_item
       - discount
       - product_price
       - quantity
       - cart_id
       - product_id

- Cart will be related to the user.
  - Each user will have one cart.
  - And cart will be related to cart_items, where cart_items will have both products that are added by all users,
  - But it will be with a reference to the cart.
  - Also, we have associated cart_items to the products table.
  - Also, there is one more thing we have product price mentioned in the product table, but we will also add the price in the cart_items, so that it will help to identify at what price the user added the item.
  
