package com.justjava.ams.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrganizationUpdateRequest {
    @NotBlank(message = "Organization name is required")
    @Size(max = 255, message = "Organization name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Registration number is required")
    @Size(max = 255, message = "Registration number must not exceed 255 characters")
    private String registrationNumber;

    @Size(max = 255, message = "Tax ID must not exceed 255 characters")
    private String taxId;

    @Size(max = 1000, message = "Address must not exceed 1000 characters")
    private String address;

    @Size(max = 255, message = "City must not exceed 255 characters")
    private String city;

    @Size(max = 255, message = "State must not exceed 255 characters")
    private String state;

    @Size(max = 255, message = "Country must not exceed 255 characters")
    private String country;

    @Size(max = 50, message = "Postal code must not exceed 50 characters")
    private String postalCode;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    private Boolean active;
}
