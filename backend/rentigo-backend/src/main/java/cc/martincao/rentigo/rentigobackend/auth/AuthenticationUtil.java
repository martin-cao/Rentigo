package cc.martincao.rentigo.rentigobackend.auth;

import cc.martincao.rentigo.rentigobackend.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 认证工具类，用于获取当前用户信息
 */
@Component
public class AuthenticationUtil {
    
    private final UserRepository userRepository;
    
    public AuthenticationUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * 获取当前登录用户 ID
     */
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new IllegalStateException("User not found: " + auth.getName()))
                    .getId();
        }
        throw new IllegalStateException("No authenticated user found");
    }
    
    /**
     * 获取当前认证对象
     */
    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
