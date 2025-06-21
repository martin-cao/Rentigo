package cc.martincao.rentigo.rentigobackend.user.service.impl;

import cc.martincao.rentigo.rentigobackend.user.Role;
import cc.martincao.rentigo.rentigobackend.user.User;
import cc.martincao.rentigo.rentigobackend.user.dto.LoginRequest;
import cc.martincao.rentigo.rentigobackend.user.dto.LoginResponse;
import cc.martincao.rentigo.rentigobackend.user.dto.PasswordChangeRequest;
import cc.martincao.rentigo.rentigobackend.user.dto.RegisterRequest;
import cc.martincao.rentigo.rentigobackend.user.dto.UserResponse;
import cc.martincao.rentigo.rentigobackend.user.exception.EmailAlreadyExistsException;
import cc.martincao.rentigo.rentigobackend.user.exception.RoleNotFoundException;
import cc.martincao.rentigo.rentigobackend.user.exception.UsernameAlreadyExistsException;
import cc.martincao.rentigo.rentigobackend.user.repository.RoleRepository;
import cc.martincao.rentigo.rentigobackend.user.repository.UserRepository;
import cc.martincao.rentigo.rentigobackend.user.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt; // For roleRepository.getReferenceById argument matching
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq; // Ensure this import is present
import static org.mockito.Mockito.mock; // Added for mocking Authentication and SecurityContext
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private User user;
    private Role userRole;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("encodedPassword");
        user.setStatus(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRole = new Role();
        userRole.setId(1); // Assuming 1 is the ID for "ROLE_USER"
        userRole.setName("ROLE_USER");
        user.setRoles(Set.of(userRole));

        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .status(0)
                .roles(Set.of("ROLE_USER"))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Test
    void register_success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        // Mock getReferenceById as it's used in the implementation
        when(roleRepository.getReferenceById(eq(1))).thenReturn(userRole); 
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(User.class), eq(UserResponse.class))).thenReturn(userResponse);

        // Act
        UserResponse actualResponse = userService.register(registerRequest);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(userResponse.getUsername(), actualResponse.getUsername());
        assertEquals(userResponse.getEmail(), actualResponse.getEmail());
        assertTrue(actualResponse.getRoles().contains("ROLE_USER"));

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        // Verify getReferenceById was called
        verify(roleRepository).getReferenceById(eq(1)); 
        verify(userRepository).save(any(User.class));
        verify(modelMapper).map(user, UserResponse.class);
    }

    @Test
    void register_usernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThrows(UsernameAlreadyExistsException.class, () -> {
            userService.register(registerRequest);
        });

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(roleRepository, never()).getReferenceById(anyInt());
        verify(userRepository, never()).save(any(User.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void register_emailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.register(registerRequest);
        });

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(roleRepository, never()).getReferenceById(anyInt());
        verify(userRepository, never()).save(any(User.class));
        verify(modelMapper, never()).map(any(), any());
    }
    
    @Test
    void register_defaultRoleNotFound() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testUser");
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        // Use eq(1) for precise argument matching and simulate RoleNotFoundException via EntityNotFoundException
        when(roleRepository.getReferenceById(eq(1)))
                .thenThrow(new EntityNotFoundException("Simulated ENE for default role not found"));

        Exception exception = assertThrows(RoleNotFoundException.class, () -> {
            userService.register(request); // Use the local request object
        });

        assertEquals("角色未找到: 1", exception.getMessage());
        verify(userRepository).existsByUsername("testUser"); // Verify with local request's username
        verify(userRepository).existsByEmail("test@example.com"); // Verify with local request's email
        verify(passwordEncoder).encode("password"); // Verify with local request's password
        verify(roleRepository).getReferenceById(1);
        verify(userRepository, never()).save(any(User.class));
        verify(modelMapper, never()).map(any(), any());
    }

    // Tests for changePassword method
    @Test
    void changePassword_Success() {
        Long userId = 1L;
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setPasswordHash("encodedOldPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.getOldPassword(), "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");

        userService.changePassword(userId, request);

        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, times(1)).matches("oldPassword", "encodedOldPassword");
        verify(passwordEncoder, times(1)).encode("newPassword");
        assertEquals("encodedNewPassword", mockUser.getPasswordHash());
    }

    @Test
    void changePassword_UserNotFound() {
        Long userId = 1L;
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword(userId, request);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void changePassword_OldPasswordMismatch() {
        Long userId = 1L;
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("wrongOldPassword");
        request.setNewPassword("newPassword");
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setPasswordHash("encodedOldPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.getOldPassword(), "encodedOldPassword")).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword(userId, request);
        });

        assertEquals("旧密码不匹配", exception.getMessage());
        verify(passwordEncoder, times(1)).matches("wrongOldPassword", "encodedOldPassword");
        verify(passwordEncoder, never()).encode(anyString());
    }

    // Tests for updateRoles method
    @Test
    void updateRoles_Success() {
        Long userId = 1L;
        Set<Integer> roleIds = Set.of(1, 2);
        User mockUser = new User();
        mockUser.setId(userId);

        Role role1 = new Role();
        role1.setId(1);
        role1.setName("USER");
        Role role2 = new Role();
        role2.setId(2);
        role2.setName("ADMIN");
        Set<Role> mockRolesSet = Set.of(role1, role2);
        List<Role> mockRolesList = new java.util.ArrayList<>(mockRolesSet); // findAllById returns List

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findAllById(roleIds)).thenReturn(mockRolesList);

        userService.updateRoles(userId, roleIds);

        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findAllById(roleIds);
        assertEquals(mockRolesSet, mockUser.getRoles());
    }

    @Test
    void updateRoles_UserNotFound() {
        Long userId = 1L;
        Set<Integer> roleIds = Set.of(1);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.updateRoles(userId, roleIds);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(roleRepository, never()).findAllById(any());
    }

    @Test
    void updateRoles_EmptyRoleIds() {
        Long userId = 1L;
        Set<Integer> roleIds = Set.of(); 
        User mockUser = new User();
        mockUser.setId(userId);
        
        Role initialRole = new Role(); // Give an initial role to check if it's cleared
        initialRole.setId(99); 
        initialRole.setName("TEMP_ROLE");
        mockUser.setRoles(Set.of(initialRole));

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findAllById(roleIds)).thenReturn(new java.util.ArrayList<>()); 

        userService.updateRoles(userId, roleIds);

        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findAllById(roleIds);
        assertTrue(mockUser.getRoles().isEmpty(), "Roles should be empty after updating with empty set of IDs");
    }

    @Test
    void updateRoles_SomeRoleIdsNotFound() {
        Long userId = 1L;
        Set<Integer> requestedRoleIds = Set.of(1, 3); // Assume roleId 3 does not exist
        User mockUser = new User();
        mockUser.setId(userId);

        Role role1 = new Role();
        role1.setId(1);
        role1.setName("USER");
        
        List<Role> existingRolesFromRepo = List.of(role1); // roleRepository.findAllById returns only existing roles
        Set<Role> expectedRolesAssignedToUser = Set.of(role1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findAllById(requestedRoleIds)).thenReturn(existingRolesFromRepo);

        userService.updateRoles(userId, requestedRoleIds);

        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findAllById(requestedRoleIds);
        assertEquals(expectedRolesAssignedToUser, mockUser.getRoles());
    }

    // Tests for register method
    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(request.getUsername());
        savedUser.setEmail(request.getEmail());

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setId(1L);
        expectedResponse.setUsername(request.getUsername());
        expectedResponse.setEmail(request.getEmail());

        Role defaultRole = new Role();
        defaultRole.setId(1); 
        defaultRole.setName("USER");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        // Use eq(1) for precise argument matching
        when(roleRepository.getReferenceById(eq(1))).thenReturn(defaultRole); 
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L); 
            return u;
        });
        when(modelMapper.map(any(User.class), eq(UserResponse.class))).thenReturn(expectedResponse);

        UserResponse actualResponse = userService.register(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getUsername(), actualResponse.getUsername());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());

        verify(userRepository, times(1)).existsByUsername("newUser");
        verify(userRepository, times(1)).existsByEmail("new@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(roleRepository, times(1)).getReferenceById(1);
        verify(userRepository, times(1)).save(any(User.class));
        verify(modelMapper, times(1)).map(any(User.class), eq(UserResponse.class));
    }

    @Test
    void register_UsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> {
            userService.register(request);
        });

        verify(userRepository, times(1)).existsByUsername("existingUser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(roleRepository, never()).getReferenceById(anyInt());
        verify(userRepository, never()).save(any(User.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void register_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.register(request);
        });

        verify(userRepository, times(1)).existsByUsername("newUser");
        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(roleRepository, never()).getReferenceById(anyInt());
        verify(userRepository, never()).save(any(User.class));
        verify(modelMapper, never()).map(any(), any());
    }

    // Add tests for login method next

    @Test
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("password");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(jwtTokenProvider.generateToken(mockUser)).thenReturn("mockToken");

        // Mock SecurityContextHolder
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        // No need to mock securityContext.setAuthentication directly, 
        // as the service method calls SecurityContextHolder.getContext().setAuthentication(auth)
        // We verify the interaction with authenticationManager and jwtTokenProvider

        LoginResponse loginResponse = userService.login(loginRequest);

        assertEquals("mockToken", loginResponse.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, times(1)).generateToken(mockUser);
        // Verify that SecurityContextHolder.getContext().setAuthentication() was effectively called
        // by checking if the authentication object was set in the context (indirectly)
        // For more direct verification, one might need to mock SecurityContextHolder itself if possible,
        // or ensure the test setup correctly reflects the state after setAuthentication.
        // Here, we trust that Spring Security's SecurityContextHolder.getContext().setAuthentication() works as expected.
    }

    @Test
    void login_AuthenticationFailure() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("wrongPassword");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            userService.login(loginRequest);
        });

        verify(jwtTokenProvider, never()).generateToken(any(User.class));
        // Ensure SecurityContext is not modified on failure
        // This is harder to verify directly without deeper mocking of SecurityContextHolder
        // but the primary check is that no token is generated.
    }

    // Add tests for me method next

    @Test
    void me_Success() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("currentUser");
        mockUser.setEmail("current@example.com");

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setId(1L);
        expectedResponse.setUsername("currentUser");
        expectedResponse.setEmail("current@example.com");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(modelMapper.map(mockUser, UserResponse.class)).thenReturn(expectedResponse);

        UserResponse actualResponse = userService.me();

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getUsername(), actualResponse.getUsername());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());

        verify(modelMapper, times(1)).map(mockUser, UserResponse.class);
    }

    @Test
    void me_NoAuthentication() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null); // No authentication object
        SecurityContextHolder.setContext(securityContext);

        assertThrows(NullPointerException.class, () -> {
            userService.me(); // This will cause NPE when getPrincipal() is called on null auth
        });
    }

    @Test
    void me_AuthenticationPrincipalNotUser() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new Object()); // Principal is not a User instance

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(ClassCastException.class, () -> {
            userService.me(); // This will cause ClassCastException
        });
    }

    // Add tests for findAll method next

    @Test
    void findAll_Success() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        List<User> users = List.of(user1, user2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        UserResponse response1 = new UserResponse();
        response1.setId(1L);
        response1.setUsername("user1");
        UserResponse response2 = new UserResponse();
        response2.setId(2L);
        response2.setUsername("user2");

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(modelMapper.map(user1, UserResponse.class)).thenReturn(response1);
        when(modelMapper.map(user2, UserResponse.class)).thenReturn(response2);

        Page<UserResponse> resultPage = userService.findAll(pageable);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals(2, resultPage.getContent().size());
        assertEquals("user1", resultPage.getContent().get(0).getUsername());
        assertEquals("user2", resultPage.getContent().get(1).getUsername());

        verify(userRepository, times(1)).findAll(pageable);
        verify(modelMapper, times(1)).map(user1, UserResponse.class);
        verify(modelMapper, times(1)).map(user2, UserResponse.class);
    }

    @Test
    void findAll_EmptyResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyUserPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(emptyUserPage);

        Page<UserResponse> resultPage = userService.findAll(pageable);

        assertNotNull(resultPage);
        assertTrue(resultPage.getContent().isEmpty());
        assertEquals(0, resultPage.getTotalElements());

        verify(userRepository, times(1)).findAll(pageable);
        verify(modelMapper, never()).map(any(), any()); // No mapping if no users
    }
}
