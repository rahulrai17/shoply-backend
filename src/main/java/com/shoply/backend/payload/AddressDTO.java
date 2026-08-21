package com.shoply.backend.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Auto-generated Address ID")
    private Long addressId;

    @NotBlank(message = "Street is required")
    @Size(min = 5, message = "Street name must be at least 5 characters")
    @Schema(description = "Street address", example = "123 Tech Boulevard")
    private String street;

    @NotBlank(message = "Building name is required")
    @Size(min = 5, message = "Building name must be at least 5 characters")
    @Schema(description = "Building name or suite number", example = "Innovation Heights")
    private String buildingName;

    @NotBlank(message = "City is required")
    @Size(min = 5, message = "City name must be at least 5 characters")
    @Schema(description = "City name", example = "San Francisco")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, message = "State name must be at least 2 characters")
    @Schema(description = "State or Province", example = "California")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(min = 2, message = "Country name must be at least 2 characters")
    @Schema(description = "Country name", example = "United States")
    private String country;

    @NotBlank(message = "Pincode is required")
    @Size(min = 6, message = "Pincode must be at least 6 characters")
    @Schema(description = "Postal code", example = "941051")
    private String pincode;
}
