package com.smartepm.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String phone;

    @NotBlank(message = "Department is required")
    private String department;

    private String designation;

    @PositiveOrZero(message = "Salary must be positive")
    private Double salary;

    private LocalDate dateOfJoining;

    private Boolean active;
}
