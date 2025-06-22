package cc.martincao.rentigo.rentigobackend.auth.controller;

import cc.martincao.rentigo.rentigobackend.user.User;
import cc.martincao.rentigo.rentigobackend.user.repository.UserRepository;
import cc.martincao.rentigo.rentigobackend.user.util.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器 - 支持JWT token生成
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, 
                         JwtTokenProvider jwtTokenProvider,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        String username = (String) request.get("username");
        String email = (String) request.get("email");
        String password = (String) request.get("password");
        
        if (username == null || email == null || password == null) {
            response.put("error", "Username, email and password are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 检查用户名是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            response.put("error", "Username already exists");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password)); // 加密密码
        user.setStatus(1); // 活跃状态
        
        User savedUser = userRepository.save(user);
        
        // 生成JWT token
        String token = jwtTokenProvider.generateToken(savedUser);
        
        response.put("id", savedUser.getId());
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());
        response.put("status", savedUser.getStatus());
        response.put("token", token);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            
            if (username == null || password == null) {
                response.put("error", "Username and password are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 尝试认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // 获取完整的User对象用于生成token
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));
            String token = jwtTokenProvider.generateToken(user);
            
            // 返回符合 OpenAPI 规范的格式
            response.put("token", token);
            
        } catch (AuthenticationException e) {
            response.put("error", "Invalid username or password");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            response.put("error", "Internal server error");
            return ResponseEntity.status(500).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Authentication service is running");
        response.put("endpoints", new String[]{"/api/auth/register", "/api/auth/login"});
        return ResponseEntity.ok(response);
    }
}
