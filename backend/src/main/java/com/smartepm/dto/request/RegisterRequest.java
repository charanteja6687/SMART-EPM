package com.smartepm.dto.request;

import com.smartepm.entity.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    // Note: there is no employeeId field here on purpose. When registering as EMPLOYEE,
    // the backend automatically finds-or-creates the matching Employee record by email
    // (see AuthServiceImpl.register) — the user never has to know or type any internal ID.
}
