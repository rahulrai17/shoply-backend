package com.shoply.backend.exceptions;

import com.shoply.backend.payload.APIResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class MyGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse> myMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> resp = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = ((FieldError) err).getField();
            String message = err.getDefaultMessage();
            resp.put(fieldName, message);
        });
        
        APIResponse apiResponse = new APIResponse("Validation Failed", false, resp);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<APIResponse> myConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> resp = new HashMap<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            // Optional: simplify "data.categoryName" to just "categoryName"
            String fieldName = propertyPath.contains(".") ? 
                    propertyPath.substring(propertyPath.lastIndexOf(".") + 1) : propertyPath;
            resp.put(fieldName, violation.getMessage());
        }

        APIResponse apiResponse = new APIResponse("Validation Failed", false, resp);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> myResponseNotFoundException(ResourceNotFoundException e) {
        String message = e.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> myAPIException(APIException e) {
        String message = e.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse> myGlobalException(Exception e) {
        // Log the full error for the dev team (use a Logger in real apps)
        e.printStackTrace();
        
        APIResponse apiResponse = new APIResponse(e.getMessage(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}