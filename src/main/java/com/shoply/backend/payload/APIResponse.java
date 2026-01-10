package com.shoply.backend.payload;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponse {
    public String message;
    private boolean status;
    private Map<String, String> details;

    public APIResponse(String message, boolean status) {
        this.message = message;
        this.status = status;
        this.details = null;
    }
}
