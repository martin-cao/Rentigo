package cc.martincao.rentigo.rentigobackend.user.service.impl;

import cc.martincao.rentigo.rentigobackend.user.User;
import cc.martincao.rentigo.rentigobackend.user.Role;
import cc.martincao.rentigo.rentigobackend.user.dto.*;
import cc.martincao.rentigo.rentigobackend.user.exception.*;
import cc.martincao.rentigo.rentigobackend.user.repository.UserRepository;
import cc.martincao.rentigo.rentigobackend.user.repository.RoleRepository;          // 你已有
import cc.martincao.rentigo.rentigobackend.user.service.UserService;
import cc.martincao.rentigo.rentigobackend.user.util.JwtTokenProvider;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwt;
    private final ModelMapper mapper;

    public UserServiceImpl(UserRepository userRepo,
                           RoleRepository roleRepo,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authManager,
                           JwtTokenProvider jwt,
                           ModelMapper mapper) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwt = jwt;
        this.mapper = mapper;
    }

    /** 注册 */
    @Override
    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRoles(Set.of(roleRepo.getReferenceById(1))); // 默认 USER
        User saved = userRepo.save(user);
        return mapper.map(saved, UserResponse.class);
    }

    /** 登录 -> JWT */
    @Override
    public String login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        User principal = (User) auth.getPrincipal();
        return jwt.generateToken(principal);
    }

    /** 当前登录用户 */
    @Override
    public UserResponse me() {
        User current = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return mapper.map(current, UserResponse.class);
    }

    /** 管理员分页查用户 */
    @Override
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepo.findAll(pageable)
                .map(u -> mapper.map(u, UserResponse.class));
    }

    /** 用户改密码 */
    @Override
    @Transactional
    public void changePassword(Long uid, PasswordChangeRequest req) {
        User user = userRepo.findById(uid)
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
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Set<Role> roles = roleRepo.findAllById(roleIds).stream().collect(Collectors.toSet());
        user.setRoles(roles);
    }
}
