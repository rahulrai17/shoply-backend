package com.shoply.backend.security;

import com.shoply.backend.model.AppRole;
import com.shoply.backend.model.Category;
import com.shoply.backend.model.Product;
import com.shoply.backend.model.Role;
import com.shoply.backend.model.User;
import com.shoply.backend.repositories.CategoryRepository;
import com.shoply.backend.repositories.ProductRepository;
import com.shoply.backend.repositories.RoleRepository;
import com.shoply.backend.repositories.UserRepository;
import com.shoply.backend.security.jwt.AuthEntryPointJwt;
import com.shoply.backend.security.jwt.AuthTokenFilter;
import com.shoply.backend.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private AuthTokenFilter authTokenFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/h2-console/**").permitAll()
                                .requestMatchers("/api/public/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/api/test/**").permitAll()
                                .requestMatchers("/images/**").permitAll()
                                .requestMatchers("/error").permitAll()
                                // Allow both ADMIN and SELLER roles to add and manage products
                                .requestMatchers("/api/admin/categories/*/product", "/api/admin/products/*").hasAnyRole("ADMIN", "SELLER")
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/categories/**").hasAnyRole("ADMIN", "SELLER")
                                .requestMatchers("/api/products/**").hasAnyRole("ADMIN", "SELLER")
                                .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().requestMatchers("/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**"));
    }

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository,
                                      UserRepository userRepository,
                                      CategoryRepository categoryRepository,
                                      ProductRepository productRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Retrieve or create default roles
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_USER)));

            Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                    .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_SELLER)));

            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_ADMIN)));

            // 2. Create default accounts with mutable HashSet collections if not already present
            if (!userRepository.existsByUserName("user1")) {
                User user1 = new User("user1", "user1@example.com", passwordEncoder.encode("password1"));
                user1.setRoles(new HashSet<>(Set.of(userRole)));
                userRepository.save(user1);
            }

            if (!userRepository.existsByUserName("seller1")) {
                User seller1 = new User("seller1", "seller1@example.com", passwordEncoder.encode("password2"));
                seller1.setRoles(new HashSet<>(Set.of(sellerRole)));
                userRepository.save(seller1);
            }

            if (!userRepository.existsByUserName("admin")) {
                User admin = new User("admin", "admin@example.com", passwordEncoder.encode("adminPass"));
                admin.setRoles(new HashSet<>(Set.of(userRole, sellerRole, adminRole)));
                userRepository.save(admin);
            }

            // 3. Seed Preset Categories if database has no categories
            Category electronicsCategory = categoryRepository.findByCategoryName("Electronics");
            if (electronicsCategory == null) {
                electronicsCategory = new Category();
                electronicsCategory.setCategoryName("Electronics");
                electronicsCategory = categoryRepository.save(electronicsCategory);
            }

            Category fashionCategory = categoryRepository.findByCategoryName("Fashion");
            if (fashionCategory == null) {
                fashionCategory = new Category();
                fashionCategory.setCategoryName("Fashion");
                fashionCategory = categoryRepository.save(fashionCategory);
            }

            // 4. Seed Preset Products owned by seller1 if product list is empty
            if (productRepository.count() == 0) {
                User sellerObj = userRepository.findByUserName("seller1").orElse(null);

                Product p1 = new Product();
                p1.setProductName("Wireless Headphones");
                p1.setDescription("High-quality noise canceling bluetooth headphones");
                p1.setImage("default.png");
                p1.setQuantity(50);
                p1.setPrice(100.0);
                p1.setDiscount(10.0);
                p1.setSpecialPrice(90.0);
                p1.setCategory(electronicsCategory);
                p1.setUser(sellerObj);
                productRepository.save(p1);

                Product p2 = new Product();
                p2.setProductName("Smart Watch Series 5");
                p2.setDescription("Waterproof fitness tracker with heart rate monitor");
                p2.setImage("default.png");
                p2.setQuantity(30);
                p2.setPrice(200.0);
                p2.setDiscount(15.0);
                p2.setSpecialPrice(170.0);
                p2.setCategory(electronicsCategory);
                p2.setUser(sellerObj);
                productRepository.save(p2);

                Product p3 = new Product();
                p3.setProductName("Classic Denim Jacket");
                p3.setDescription("Premium quality vintage style denim jacket");
                p3.setImage("default.png");
                p3.setQuantity(20);
                p3.setPrice(80.0);
                p3.setDiscount(5.0);
                p3.setSpecialPrice(76.0);
                p3.setCategory(fashionCategory);
                p3.setUser(sellerObj);
                productRepository.save(p3);
            }
        };
    }
}
