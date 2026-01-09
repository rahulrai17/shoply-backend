# Default file
- In this we have the file which is from the Spring security side which is used for default configuration.

```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.boot.autoconfigure.security.servlet;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnWebApplication(
    type = Type.SERVLET
)
class SpringBootWebSecurityConfiguration {
    SpringBootWebSecurityConfiguration() {
    }

    @Configuration(
        proxyBeanMethods = false
    )
    @ConditionalOnMissingBean(
        name = {"springSecurityFilterChain"}
    )
    @ConditionalOnClass({EnableWebSecurity.class})
    @EnableWebSecurity
    static class WebSecurityEnablerConfiguration {
        WebSecurityEnablerConfiguration() {
        }
    }

    @Configuration(
        proxyBeanMethods = false
    )
    @ConditionalOnDefaultWebSecurity
    static class SecurityFilterChainConfiguration {
        SecurityFilterChainConfiguration() {
        }

        @Bean
        @Order(2147483642)
        SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests((requests) -> {
                ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)requests.anyRequest()).authenticated();
            });
            http.formLogin(Customizer.withDefaults());
            http.httpBasic(Customizer.withDefaults());
            return (SecurityFilterChain)http.build();
        }
    }
}

```

This is a **Spring Boot auto-configuration class** that defines the **default security setup** when Spring Security is included in a Spring Boot project. This file is part of the **Spring Boot Autoconfiguration** mechanism, which means that it automatically configures Spring Security when certain conditions are met.

Let’s break down each section step-by-step:

---

## **1. Package Declaration**
```java
package org.springframework.boot.autoconfigure.security.servlet;
```
- This file is part of the `org.springframework.boot.autoconfigure.security.servlet` package.
- The `servlet` subpackage indicates that this configuration is specifically for **servlet-based web applications** (not reactive).

---

## **2. Class Declaration**
```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
class SpringBootWebSecurityConfiguration {
```
### 🔎 **@Configuration**
- Marks this class as a Spring configuration class.
- Spring will scan and register this class as part of the application context.

### 🕵️ **@ConditionalOnWebApplication(type = Type.SERVLET)**
- This configuration is **only enabled** if the application is a servlet-based web application (i.e., not a reactive web application like WebFlux).

### ➡️ **Class Purpose**
- `SpringBootWebSecurityConfiguration` is a container class that includes nested classes for actual security configuration.

---

## **3. Nested Class: WebSecurityEnablerConfiguration**
```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(name = {"springSecurityFilterChain"})
@ConditionalOnClass({EnableWebSecurity.class})
@EnableWebSecurity
static class WebSecurityEnablerConfiguration {
    WebSecurityEnablerConfiguration() {
    }
}
```

### ✅ **Purpose**
This class enables Spring Security when the following conditions are met:
1. `@ConditionalOnMissingBean(name = {"springSecurityFilterChain"})`  
   → If no `springSecurityFilterChain` bean is already defined, Spring Security will create a default one.
2. `@ConditionalOnClass({EnableWebSecurity.class})`  
   → If `EnableWebSecurity` class is available in the classpath (ensuring Spring Security is present).
3. `@EnableWebSecurity`  
   → Enables Spring Security's default web security setup.

### 🔥 **Role**
This class ensures that the Spring Security infrastructure is set up **ONLY IF** no custom configuration is defined.

---

## **4. Nested Class: SecurityFilterChainConfiguration**
```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnDefaultWebSecurity
static class SecurityFilterChainConfiguration {
    SecurityFilterChainConfiguration() {
    }

    @Bean
    @Order(2147483642)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) -> {
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)requests.anyRequest()).authenticated();
        });
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(Customizer.withDefaults());
        return (SecurityFilterChain)http.build();
    }
}
```

### ✅ **Purpose**
This class defines the **default security filter chain** when no custom `SecurityFilterChain` is defined.

### 🔎 **@ConditionalOnDefaultWebSecurity**
- Only enables this configuration if **default security settings** should be applied.
- Skips it if there’s a custom `SecurityFilterChain` bean defined.

### 🔢 **@Order(2147483642)**
- Sets a very high order value to make sure this filter chain is one of the **last ones** to be considered.
- Custom-defined `SecurityFilterChain` beans with lower order values will override this one.

### **Default Security Setup**
1. **Authorization**
```java
requests.anyRequest().authenticated()
```
- All HTTP endpoints require authentication by default.

2. **Form-Based Login**
```java
http.formLogin(Customizer.withDefaults());
```
- Enables a default login form for user authentication.

3. **HTTP Basic Authentication**
```java
http.httpBasic(Customizer.withDefaults());
```
- Enables HTTP Basic authentication (for API testing, etc.).

4. **Building the Security Filter Chain**
```java
return (SecurityFilterChain) http.build();
```
- Finalizes the `SecurityFilterChain` and registers it as a Spring bean.

---


## *How It All Works Together**
1. When Spring Boot starts:
    - If `spring-boot-starter-security` is on the classpath → Spring Security auto-configuration is triggered.

2. If **no custom security configuration** exists:
    - `WebSecurityEnablerConfiguration` enables Spring Security.
    - `SecurityFilterChainConfiguration` sets up default security rules.

3. If a custom `SecurityFilterChain` bean exists:
    - Spring **skips the default configuration**.

---

##  **Why This is Important**
- This file is why Spring Security "just works" out of the box in Spring Boot.
- If you want to customize security settings, you can define your own `SecurityFilterChain` bean — which will override the default configuration.

