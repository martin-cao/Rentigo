package cc.martincao.rentigo.rentigobackend.user.controller;

import cc.martincao.rentigo.rentigobackend.user.dto.*;
import cc.martincao.rentigo.rentigobackend.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    /** 当前用户 */
    @GetMapping("/users/me")
    @SecurityRequirement(name = "bearerAuth")
    public UserResponse me() {
        return users.me();
    }

    /** 管理员分页查用户 */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    @SecurityRequirement(name = "bearerAuth")
    public Page<UserResponse> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return users.findAll(PageRequest.of(page, size));
    }

    /** 管理员改角色 */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/users/{id}/roles")
    @SecurityRequirement(name = "bearerAuth")
    public void updateRoles(@PathVariable Long id,
                            @RequestBody Set<Integer> roleIds) {
        users.updateRoles(id, roleIds);
    }

    /** 用户改密码 */
    @PutMapping("/users/{id}/password")
    @SecurityRequirement(name = "bearerAuth")
    public void changePwd(@PathVariable Long id,
                          @Valid @RequestBody PasswordChangeRequest req) {
        users.changePassword(id, req);
    }
}
