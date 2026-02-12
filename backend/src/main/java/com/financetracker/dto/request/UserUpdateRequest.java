package com.financetracker.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

/**
 * Request DTO for updating user profile
 * Validates user profile update fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    @JsonProperty("first_name")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    @JsonProperty("last_name")
    private String lastName;
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Pattern(regexp = "^[\\d\\s\\-\\+\\(\\)]*$|^$", message = "Phone number format is invalid")
    @JsonProperty("phone_number")
    private String phoneNumber;
    
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
    
    @JsonProperty("profile_picture_url")
    @Pattern(regexp = "^(https?:\\/\\/.+)?$|^$", message = "Profile picture URL must be a valid URL")
    private String profilePictureUrl;
}
