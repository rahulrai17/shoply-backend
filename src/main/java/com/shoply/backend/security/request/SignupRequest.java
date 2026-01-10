package com.shoply.backend.security.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    @Schema(example = "john_doe")
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    @Schema(example = "john@example.com")
    private String email;

    @Schema(example = "[\"user\"]")
    private Set<String> role;

    @NotBlank
    @Size(min = 6, max = 40)
    @Schema(example = "password123")
    private String password;

    public Set<String> getRole() {
        return this.role;
    }

    public void setRole(Set<String> role) {
        this.role = role;
    }
}