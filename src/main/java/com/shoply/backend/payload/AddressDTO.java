package com.shoply.backend.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    @Schema(example = "1")
    private Long addressId;
    
    @Schema(example = "Main Street")
    private String street;
    
    @Schema(example = "Apt 4B")
    private String buildingName;
    
    @Schema(example = "New York")
    private String city;
    
    @Schema(example = "NY")
    private String state;
    
    @Schema(example = "USA")
    private String country;
    
    @Schema(example = "100010")
    private String pincode;
}

