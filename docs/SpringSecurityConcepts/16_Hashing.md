# **Hashing – Complete Notes**

## **1. Introduction to Hashing**
Hashing is the process of converting an input of any size into a fixed-length string using a mathematical algorithm. The output of a hashing function is known as a **hash value**, **digest**, or **hash code**.

### **Purpose of Hashing**
- **Data Integrity** – Ensure that data has not been modified.
- **Password Storage** – Securely store passwords using irreversible hashes.
- **Digital Signatures** – Verify authenticity and integrity of messages and data.
- **Data Retrieval** – Hashing allows fast lookups in data structures (e.g., hash tables).


## **2. How Hashing Works**
1. A hashing algorithm takes an input string of any length.
2. It processes the input using mathematical transformations.
3. A fixed-size hash value is generated as the output.

### **Example:**
Input → `"hello"`  
Hash Algorithm → SHA-256  
Output (Hash) →  
`2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824`


## **3. Properties of a Good Hashing Algorithm**
### 1. **Deterministic**
- The same input should always produce the same hash value.  
  Example:  
  SHA-256("hello") → `2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824`

  
### 2. **Fixed Length**
- Regardless of input size, the hash value should always have a fixed length.  
  Example:  
  SHA-256("apple") → 256-bit value  
  SHA-256("I love programming") → 256-bit value


### 3. **Fast Computation**
- Hashing should be fast for any input size, but some hashing algorithms (like bcrypt) are intentionally slow to resist brute-force attacks.



### 4. **Pre-image Resistance (One-way Property)**
- It should be computationally impossible to reverse-engineer the original input from the hash value.  
  Example:  
  If `hash(x) = 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824`,  
  you should not be able to calculate `x` from the hash.


### 5. **Collision Resistance**
- Two different inputs should not produce the same hash value.  
  Example:  
  If `hash("apple") = x12345` and `hash("orange") = x12345` → Collision (This should not happen)

### 6. **Avalanche Effect**
- A small change in input should cause a large, unpredictable change in the output hash.  
  Example:
```text
hash("hello") = 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824
hash("hella") = b6a74c5f89e067d5cf78a1c44fa96fb69f90c4bb70a3d1dcbbccd3d8ad29bcee
```


## **4. Common Hashing Algorithms**
### **4.1 MD5 (Message Digest Algorithm 5)**
- Developed by **Ronald Rivest** in 1991.
- Produces a **128-bit hash value** (32 hexadecimal characters).
- Uses **512-bit blocks** and processes data in **64 rounds**.
- Uses **nonlinear functions** like AND, OR, and XOR.

#### **Example:**
```java
import java.security.MessageDigest;

public class MD5Example {
    public static void main(String[] args) throws Exception {
        String input = "hello";
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(input.getBytes());
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        
        System.out.println("MD5 Hash: " + hexString.toString());
    }
}
```

#### **Weaknesses**
- Vulnerable to **collision attacks**.
- No longer suitable for cryptographic use.
- Still used for non-cryptographic purposes (e.g., file checksums).

### **4.2 SHA-1 (Secure Hash Algorithm 1)**
- Developed by **NSA** in 1993.
- Produces a **160-bit hash value** (40 hexadecimal characters).
- Uses **512-bit blocks** and processes data in **80 rounds**.
- Broken by collision attacks in **2017**.

#### **Example:**
```java
MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
byte[] result = sha1.digest("hello".getBytes());
System.out.println("SHA-1 Hash: " + new String(result));
```

#### **Weaknesses**
- Vulnerable to collisions.
- No longer recommended for cryptographic use.

### **4.3 SHA-256 (Secure Hash Algorithm 256)**
- Part of the **SHA-2 family** developed by NSA in 2001.
- Produces a **256-bit hash value** (64 hexadecimal characters).
- Uses **512-bit blocks** and processes data in **64 rounds**.

#### **Example:**
```java
MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
byte[] result = sha256.digest("hello".getBytes());
System.out.println("SHA-256 Hash: " + new String(result));
```

#### **Strengths**
- No known vulnerabilities.
- Widely used in SSL/TLS, Bitcoin, and secure communications.


### **4.4 SHA-512 (Secure Hash Algorithm 512)**
- Part of the **SHA-2 family**.
- Produces a **512-bit hash value** (128 hexadecimal characters).
- Uses **1024-bit blocks** and processes data in **80 rounds**.

#### **Example:**
```java
MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
byte[] result = sha512.digest("hello".getBytes());
System.out.println("SHA-512 Hash: " + new String(result));
```

#### **Strengths**
- Stronger than SHA-256 due to larger bit size.
- Used for high-security applications.


### **4.5 Bcrypt**
- Based on the **Blowfish cipher**.
- Introduces a **salt** to prevent rainbow table attacks.
- Adjustable **work factor** to control computational difficulty.

#### **Example:**
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode("password");
System.out.println("BCrypt Hash: " + hashedPassword);
```

#### **Strengths**
- Resistant to brute-force attacks.
- Uses automatic salting.


### **4.6 Argon2**
- Winner of the **Password Hashing Competition** (PHC) in 2015.
- Memory-hard algorithm → Resistant to GPU-based attacks.
- Configurable to control memory, time, and parallelism.

#### **Example:**
```java
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

Argon2PasswordEncoder encoder = new Argon2PasswordEncoder();
String hashedPassword = encoder.encode("password");
System.out.println("Argon2 Hash: " + hashedPassword);
```

#### **Strengths**
- Best choice for password hashing.
- Highly resistant to brute-force and side-channel attacks.

## **5. Salting and Peppering**
### **Salting**
- Adding a random value (salt) to the input before hashing.
- Ensures that even identical passwords produce different hashes.

Example:
```text
hash("password" + "salt123")
```

### **Peppering**
- Adding a secret key (pepper) to the input before hashing.
- Stored separately from the user database.

Example:
```text
hash("password" + "SECRET_KEY")
```


## **6. Recommended Use Cases**
| Algorithm | Use Case |
|---|---|
| SHA-256 | Data Integrity, Digital Signatures |
| SHA-512 | High-Security Requirements |
| Bcrypt | Password Storage |
| Argon2 | Modern Password Storage | 


## **7. Conclusion**
- For general security → Use SHA-256 or SHA-512.
- For password storage → Use Bcrypt or Argon2.
- Avoid MD5 and SHA-1 for cryptographic purposes.  