package cc.martincao.rentigo.rentigobackend.user.service;

import cc.martincao.rentigo.rentigobackend.user.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface UserService {
    UserResponse register(RegisterRequest request);
    String        login(LoginRequest request);
    UserResponse  me();
    Page<UserResponse> findAll(Pageable pageable);
    void changePassword(Long userId, PasswordChangeRequest request);
    void updateRoles(Long userId, Set<Integer> roleIds);
}
