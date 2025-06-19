package cc.martincao.rentigo.rentigobackend.user.service.impl;

import cc.martincao.rentigo.rentigobackend.user.User;
import cc.martincao.rentigo.rentigobackend.user.Role;
import cc.martincao.rentigo.rentigobackend.user.dto.*;
import cc.martincao.rentigo.rentigobackend.user.exception.*;
import cc.martincao.rentigo.rentigobackend.user.repository.UserRepository;
import cc.martincao.rentigo.rentigobackend.user.repository.RoleRepository;
import cc.martincao.rentigo.rentigobackend.user.service.UserService;
import cc.martincao.rentigo.rentigobackend.user.util.JwtTokenProvider;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         AuthenticationManager authenticationManager,
                         JwtTokenProvider jwtTokenProvider,
                         ModelMapper modelMapper,
                         RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.modelMapper = modelMapper;
        this.roleRepository = roleRepository;
    }

    /** 注册 */
    @Override
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Default user role not found."));
        user.setRoles(new HashSet<>(Set.of(userRole)));

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponse.class);
    }

    /** 登录 -> JWT */
    @Override
    public LoginResponse login(LoginRequest req) {
        // 1. 使用 Spring Security 的 AuthenticationManager 进行认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        // 2. 将认证信息设置到 SecurityContext 中
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. 【关键步骤】根据用户名从数据库重新获取完整的 User 对象
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("认证成功后，未找到用户：" + req.getUsername()));

        // 4. 使用这个从数据库获取的、信息完整的 User 对象生成 Token
        String token = jwtTokenProvider.generateToken(user);

        // 5. 将 Token 包装在 LoginResponse 对象中返回
        return new LoginResponse(token);
    }

    /** 当前登录用户 */
    @Override
    public UserResponse me() {
        User current = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return modelMapper.map(current, UserResponse.class);
    }

    /** 管理员分页查用户 */
    @Override
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(u -> modelMapper.map(u, UserResponse.class));
    }

    /** 用户改密码 */
    @Override
    @Transactional
    public void changePassword(Long uid, PasswordChangeRequest req) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("旧密码不匹配");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
    }

    /** 管理员变更角色 */
    @Override
    @Transactional
    public void updateRoles(Long uid, Set<Integer> roleIds) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
        user.setRoles(roles);
    }
}
