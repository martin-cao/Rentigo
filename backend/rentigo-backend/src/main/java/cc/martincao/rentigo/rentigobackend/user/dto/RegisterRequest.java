package cc.martincao.rentigo.rentigobackend.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 32)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 64)
    private String password;
}