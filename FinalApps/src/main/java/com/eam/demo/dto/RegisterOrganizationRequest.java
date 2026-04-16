package com.eam.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterOrganizationRequest {

    @NotBlank
    @Size(max = 120)
    private String organizationName;

    @NotBlank
    @Size(max = 30)
    private String organizationCode;

    @NotBlank
    @Size(max = 120)
    private String adminName;

    @NotBlank
    @Email
    @Size(max = 160)
    private String adminEmail;

    @NotBlank
    @Size(min = 8, max = 100)
    private String adminPassword;
}
