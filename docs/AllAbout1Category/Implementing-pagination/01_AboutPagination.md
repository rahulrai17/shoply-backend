# What is pagination and how to implement pagination?

- Pagination is a technique used in backend systems to divide large datasets into smaller, manageable chunks (or "pages"). It improves performance and enhances the user experience by reducing the amount of data sent in a single response.

## How to implement pagination.

- ### Step by Step Explanation of pagination in the backend :
  - #### Step 1: Understanding the Need for Pagination
    - Imagine a database with 1,000,000+ user records.
    - Fetching all users in one request would overload the server and slow down response times.
    - Solution? Pagination! Fetching a limited number of records per request improves efficiency.
  
  - #### Step 2 : Client Request with Pagination Parameters 
    - The frontend requests a specific set of records by sending pagination parameters in the API request.
    - Example :
    ```http
        GET /api/users?page=2&limit=10 
    ```
    - `page=2` means the client is requesting the second page of data
    - `limit=10` means the client wants 10 records per page.
  
  - #### Step 3 : Backend Processes the Request 
    - The backend extracts the pagination parameters from the request.
    - Example :
    ```java
    @RestController
    @RequestMapping("/api/users")
    public class UserController {

        @Autowired
        private UserRepository userRepository;
    
        @GetMapping
        public ResponseEntity<Map<String, Object>> getUsers(
                @RequestParam(defaultValue = "1") int page,
                @RequestParam(defaultValue = "10") int limit) {
    
            // Adjusting page number for zero-based index
            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<User> usersPage = userRepository.findAll(pageable);
    
            // Constructing the response
            Map<String, Object> response = new HashMap<>();
            response.put("page", page);
            response.put("limit", limit);
            response.put("total_pages", usersPage.getTotalPages());
            response.put("total_records", usersPage.getTotalElements());
            response.put("data", usersPage.getContent());
    
            return ResponseEntity.ok(response);
        }
    }

    ```