package cc.martincao.rentigo.rentigobackend.user.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Integer status;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
