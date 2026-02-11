package com.financetracker.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Request DTO for updating user profile
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    @NotBlank(message = "First name is required")
    @JsonProperty("first_name")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @JsonProperty("last_name")
    private String lastName;
    
    @Email(message = "Email should be valid")
    @NotNull(message = "Email is required")
    private String email;
    
    @JsonProperty("phone_number")
    private String phoneNumber;
    
    @JsonProperty("profile_picture_url")
    private String profilePictureUrl;
}
