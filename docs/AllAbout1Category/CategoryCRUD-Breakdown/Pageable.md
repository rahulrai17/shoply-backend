# Pageable class and how it works :

Great question! Let’s dive deep into how **Spring Data JPA's `Pageable`** works, including its internal behavior, whether it loads all data at once, and how it interacts with the database.

---

## **Understanding `Pageable` in Spring Data JPA**
Spring Data JPA provides `Pageable` to implement **efficient pagination and sorting**. It does **not** load all data into memory at once but instead makes **separate queries** to fetch only the required subset of data.

---

## **📌 Step-by-Step Working of `Pageable`**
### **1️⃣ User Requests Paginated Data**
When the client requests paginated data, they typically send parameters like:
```plaintext
pageNumber = 1
pageSize = 5
sortBy = "categoryName"
sortOrder = "asc"
```
The method in the repository is called:
```java
Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending());
Page<Category> categoryPage = categoryRepository.findAll(pageable);
```
---
### **2️⃣ Spring Data JPA Generates a SQL Query**
Spring Data JPA **does not load all data** into memory. Instead, it converts the request into a SQL query that fetches **only the required page of records**.

If we assume a **table `categories`** with **20 records**, the query generated for **pageNumber = 1 and pageSize = 5** (zero-based indexing) would be:

```sql
SELECT * FROM categories ORDER BY category_name ASC LIMIT 5 OFFSET 5;
```
🔍 **Breakdown of Query:**
- `ORDER BY category_name ASC` → Sorts by `category_name` in ascending order.
- `LIMIT 5` → Fetches **only 5 records**.
- `OFFSET 5` → Skips the **first 5 records** (because page index 1 starts at record 6).

👉 **Key takeaway**: **Only the requested page data is fetched from the database, NOT all records.**

---

### **3️⃣ Spring Maps the Data to a `Page` Object**
The result from the database is mapped into a `Page<Category>` object containing:
- **Actual data (`List<Category>`)**
- **Metadata** about pagination

The `Page<Category>` contains:
```java
categoryPage.getContent(); // The actual list of categories
categoryPage.getTotalElements(); // Total number of records in DB (20 in our example)
categoryPage.getTotalPages(); // Total pages (totalElements / pageSize)
categoryPage.getNumber(); // Current page index (zero-based)
categoryPage.getSize(); // Page size (records per page)
categoryPage.isLast(); // Is this the last page?
```

Example response:
```json
{
  "content": [
    { "categoryID": 6, "categoryName": "Books" },
    { "categoryID": 7, "categoryName": "Clothing" },
    { "categoryID": 8, "categoryName": "Electronics" },
    { "categoryID": 9, "categoryName": "Furniture" },
    { "categoryID": 10, "categoryName": "Toys" }
  ],
  "pageNumber": 1,
  "pageSize": 5,
  "totalElements": 20,
  "totalPages": 4,
  "lastPage": false
}
```

---

## **📌 Does `Pageable` Load All Data at Once?**
🚫 **No! It does NOT load all data into memory.**  
✅ Instead, Spring Data JPA **only retrieves the required subset** of records using `LIMIT` and `OFFSET`.

### **🔥 What Happens Internally?**
1. **Query Execution**: A SQL query with `LIMIT` and `OFFSET` is run to fetch only **one page of records**.
2. **Total Count Query**: If required (e.g., to calculate total pages), Spring Data JPA executes:
   ```sql
   SELECT COUNT(*) FROM categories;
   ```
   This helps determine the `totalElements` count.

3. **Mapping**: The retrieved records are **mapped into DTOs or Entities**.

🚀 **Efficiency**: Since the database does the filtering, sorting, and limiting, only the required data is loaded into memory, making it efficient for large datasets.

---

## **📌 Does It Make a New Query for Each Page Request?**
✅ **Yes, every page request triggers a new query.**  
Each time the user requests a new page (`pageNumber` changes), Spring Data JPA sends **a fresh query to the database** with updated `LIMIT` and `OFFSET` values.

**Example Queries for Different Page Requests**
| Page Number | Page Size | Query |
|------------|----------|--------------------------------------|
| 0 (first)  | 5        | `SELECT * FROM categories ORDER BY category_name ASC LIMIT 5 OFFSET 0;` |
| 1 (second) | 5        | `SELECT * FROM categories ORDER BY category_name ASC LIMIT 5 OFFSET 5;` |
| 2 (third)  | 5        | `SELECT * FROM categories ORDER BY category_name ASC LIMIT 5 OFFSET 10;` |

---

## **📌 What If There Are No More Pages?**
If the user requests a page that **doesn’t exist**, the `Page` object will return an **empty list**.

```json
{
  "content": [],
  "pageNumber": 4,
  "pageSize": 5,
  "totalElements": 20,
  "totalPages": 4,
  "lastPage": true
}
```
**🔍 How does Spring handle this?**
- `categoryPage.getContent().isEmpty()` returns `true` if there are no records.
- `categoryPage.isLast()` will return `true` for the last page.

---

## **📌 What If the `pageSize` Is Large?**
If a user requests a very large `pageSize`, like `10000`, it **can impact performance** because:
- **More data is fetched** in a single query.
- **More memory is used** on the application server.
- **Database load increases**.

👉 To avoid performance issues, you can **set a max page size limit**:
```java
int maxPageSize = 100;
pageSize = Math.min(pageSize, maxPageSize);
```
This prevents users from requesting unreasonably large pages.

---

## **📌 Summary**
| ✅ Feature | ✅ Behavior |
|------------|------------|
| Does `Pageable` load all data? | ❌ No, it fetches only the required subset. |
| How many queries are executed? | 🟢 One query per page request (`LIMIT OFFSET`). |
| Does it count total records? | 🟢 Yes, a `COUNT(*)` query is run when needed. |
| Does `Pageable` improve performance? | ✅ Yes, by **fetching only required records**. |
| What happens if no records exist? | ❌ Returns an empty `Page` object. |

---

## **🚀 Final Thoughts**
- **Spring Data JPA handles pagination efficiently** by using `LIMIT` and `OFFSET`, ensuring that only necessary data is fetched.
- **It does NOT load all records into memory**, preventing performance issues.
- **Each page request triggers a fresh query**, meaning the database only fetches relevant records.

Would you like a **code example** showing how the repository method and controller work together for pagination? 🚀