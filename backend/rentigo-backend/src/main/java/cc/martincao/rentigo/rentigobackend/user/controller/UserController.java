package cc.martincao.rentigo.rentigobackend.user.controller;

import cc.martincao.rentigo.rentigobackend.user.dto.*;
import cc.martincao.rentigo.rentigobackend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    /** 注册 */
    @PostMapping("/auth/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest req) {
        return users.register(req);
    }

    /** 登录 */
    @PostMapping("/auth/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest req) {
        return Map.of("token", users.login(req));
    }

    /** 当前用户 */
    @GetMapping("/users/me")
    public UserResponse me() {
        return users.me();
    }

    /** 管理员分页查用户 */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public Page<UserResponse> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return users.findAll(PageRequest.of(page, size));
    }

    /** 管理员改角色 */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/users/{id}/roles")
    public void updateRoles(@PathVariable Long id,
                            @RequestBody Set<Integer> roleIds) {
        users.updateRoles(id, roleIds);
    }

    /** 用户改密码 */
    @PutMapping("/users/{id}/password")
    public void changePwd(@PathVariable Long id,
                          @Valid @RequestBody PasswordChangeRequest req) {
        users.changePassword(id, req);
    }
}
