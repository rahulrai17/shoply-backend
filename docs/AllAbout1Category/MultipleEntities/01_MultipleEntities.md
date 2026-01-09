# **Entity Relationships in Java Spring Boot**
Entity relationships in a **Spring Boot** application are an essential part of designing a relational database structure. When working with **JPA (Java Persistence API)** and **Hibernate**, we need to establish relationships between different entities to reflect real-world scenarios.

---

## **1. Understanding Entities in Spring Boot**
An **entity** in Spring Boot represents a table in the database. It is a Java class annotated with `@Entity`, and each instance corresponds to a row in the database.

### **Example of an Entity**
```java
import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Getters and Setters
}
```
### **Key Annotations Used**
- `@Entity` → Marks the class as a JPA entity.
- `@Table(name = "students")` → Specifies the table name.
- `@Id` → Marks the primary key field.
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` → Auto-generates primary key values.
- `@Column(nullable = false, unique = true)` → Defines column constraints.

---

## **2. Types of Entity Relationships**
### **Entity relationships define how multiple entities relate to each other in the database. There are three main types:**
1. **One-to-One (`@OneToOne`)**
2. **One-to-Many / Many-to-One (`@OneToMany`, `@ManyToOne`)**
3. **Many-to-Many (`@ManyToMany`)**

---

## **3. One-to-One Relationship (`@OneToOne`)**
A one-to-one relationship means that one entity is related to only one other entity.

### **Use Case**
A **student** has only **one address**.

### **Example Implementation**
```java
import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private String state;
    
    @OneToOne
    @JoinColumn(name = "student_id", unique = true)
    private Student student;

    // Getters and Setters
}
```
### **Key Annotations Used**
- `@OneToOne` → Defines a one-to-one relationship.
- `@JoinColumn(name = "student_id", unique = true)` → Creates a foreign key column in the `addresses` table, ensuring uniqueness.

---

## **4. One-to-Many / Many-to-One Relationship**
A **one-to-many** relationship means that one entity is related to multiple entities. The reverse is **many-to-one**.

### **Use Case**
A **teacher** can have multiple **students**, but each **student** has only one **teacher**.

### **Example Implementation**
#### **Teacher Entity (One-to-Many)**
```java
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Student> students;

    // Getters and Setters
}
```

#### **Student Entity (Many-to-One)**
```java
import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    // Getters and Setters
}
```

### **Key Annotations Used**
- `@OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)`
    - `mappedBy = "teacher"` → This is the field name in the `Student` entity that owns the relationship.
    - `cascade = CascadeType.ALL` → When a teacher is saved or deleted, the students are also updated accordingly.
    - `orphanRemoval = true` → If a student is removed from the list, it will be deleted from the database.

- `@ManyToOne` → Defines the many-to-one side of the relationship.
- `@JoinColumn(name = "teacher_id")` → Specifies the foreign key column.

---

## **5. Many-to-Many Relationship (`@ManyToMany`)**
A **many-to-many** relationship means that multiple entities from both sides are related.

### **Use Case**
A **student** can enroll in multiple **courses**, and a **course** can have multiple **students**.

### **Example Implementation**
#### **Student Entity**
```java
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses;

    // Getters and Setters
}
```

#### **Course Entity**
```java
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students;

    // Getters and Setters
}
```

### **Key Annotations Used**
- `@ManyToMany` → Defines a many-to-many relationship.
- `@JoinTable(name = "student_course", joinColumns = @JoinColumn(name = "student_id"), inverseJoinColumns = @JoinColumn(name = "course_id"))`
    - Specifies the **join table** for the relationship.
    - `joinColumns` → Foreign key for the **Student** entity.
    - `inverseJoinColumns` → Foreign key for the **Course** entity.
- `mappedBy = "courses"` → Indicates that the `Student` entity owns the relationship.

---

## **6. Cascade Types in JPA**
Cascade types define how operations (like save, delete, update) are propagated.

### **Common Cascade Types**
| Cascade Type        | Description |
|---------------------|-------------|
| `CascadeType.ALL`   | All operations (Persist, Merge, Remove, Refresh, Detach) are cascaded. |
| `CascadeType.PERSIST` | When saving the parent, save the child as well. |
| `CascadeType.MERGE` | When merging parent, merge child as well. |
| `CascadeType.REMOVE` | When deleting parent, delete child as well. |
| `CascadeType.REFRESH` | When refreshing parent, refresh child as well. |
| `CascadeType.DETACH` | When detaching parent, detach child as well. |

---

## **7. Fetch Types in JPA**
JPA provides two fetch types to optimize performance.

### **FetchType.LAZY (Default for One-to-Many & Many-to-Many)**
- Loads related entities **only when accessed**.
- Improves performance by reducing unnecessary queries.

### **FetchType.EAGER (Default for One-to-One & Many-to-One)**
- Loads related entities **immediately**.
- Can cause **performance issues** if used on large datasets.

### **Example Usage**
```java
@OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
private List<Student> students;
```
Use `FetchType.LAZY` in most cases to improve performance.

---

## **Conclusion**
Entity relationships in Spring Boot using JPA and Hibernate are crucial for defining a well-structured database. The right relationship type ensures **data integrity**, improves **query performance**, and helps maintain **efficient application architecture**.

Would you like to see a working **Spring Boot CRUD example** with these relationships? 

## Directionality in Entity Relationships (JPA & Hibernate)


---
---

# **Directionality in Entity Relationships (JPA & Hibernate)**
Directionality in JPA relationships defines how entities reference each other. It determines whether the relationship is **unidirectional** or **bidirectional**.

---

## **1. Unidirectional Relationships**
A **unidirectional** relationship means that only one entity is aware of the relationship. The other entity does not have a reference back.

### **Example: One-to-One (Unidirectional)**
A **Student** has an **Address**, but the **Address** does not know about the **Student**.

```java
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address; // Only Student knows about Address

    // Getters and Setters
}
```

```java
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String city;
    private String state;

    // No reference to Student (Unidirectional)
}
```
### **Key Features**
✅ Simple and easy to manage.  
❌ The **Address** entity does not know which **Student** it belongs to.  
❌ Querying in the opposite direction (from `Address` to `Student`) is **not possible** without additional queries.

---

## **2. Bidirectional Relationships**
A **bidirectional** relationship means both entities are aware of the relationship and have references to each other.

### **Example: One-to-One (Bidirectional)**
A **Student** has an **Address**, and the **Address** also knows about the **Student**.

```java
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL)
    private Address address;

    // Getters and Setters
}
```

```java
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String city;
    private String state;

    @OneToOne
    @JoinColumn(name = "student_id")
    private Student student;

    // Getters and Setters
}
```

### **Key Features**
✅ Allows easy **bidirectional navigation** (from `Student` to `Address` and vice versa).  
✅ Helps **optimize performance** by reducing queries.  
❌ More **complex** due to two-way references.

---

## **3. Directionality in Other Relationships**
### **One-to-Many & Many-to-One**
**Unidirectional One-to-Many**  
A `Teacher` has multiple `Students`, but `Student` doesn’t know about `Teacher`.

```java
@Entity
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "teacher_id") // Creates a foreign key column in Student table
    private List<Student> students;
}
```

**Bidirectional One-to-Many (Preferred)**
A `Teacher` has multiple `Students`, and each `Student` knows its `Teacher`.

```java
@Entity
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<Student> students;
}
```

```java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;
}
```

### **Many-to-Many**
#### **Unidirectional Many-to-Many**
Each `Student` can enroll in multiple `Courses`, but the `Course` doesn’t track students.

```java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToMany
    @JoinTable(name = "student_course", 
               joinColumns = @JoinColumn(name = "student_id"), 
               inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> courses;
}
```

#### **Bidirectional Many-to-Many**
Both `Student` and `Course` track each other.

```java
@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students;
}
```

### **Which Directionality to Use?**
| Relationship Type | Unidirectional | Bidirectional |
|------------------|---------------|--------------|
| **One-to-One** | Simple, but limits navigation. | Preferred for full access. |
| **One-to-Many** | Useful if the child doesn’t need a reference. | Preferred in most cases. |
| **Many-to-Many** | Works but limits data access. | Preferred for full navigation. |

---

## **Conclusion**
**Directionality** defines **how entities reference each other**.
- **Unidirectional**: Simpler, but limits navigation.
- **Bidirectional**: More powerful, but complex.
- **Best Practice**: Choose based on **query needs** and **performance considerations**.

Would you like an example **REST API** using these relationships? 🚀

