package cc.martincao.rentigo.rentigobackend.user.controller;

import cc.martincao.rentigo.rentigobackend.user.dto.LoginRequest;
import cc.martincao.rentigo.rentigobackend.user.dto.RegisterRequest;
import cc.martincao.rentigo.rentigobackend.user.dto.UserResponse;
import cc.martincao.rentigo.rentigobackend.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;


@WebMvcTest(value = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private UserResponse userResponse;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .status(0)
                .roles(Set.of("ROLE_USER"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser"); // Changed from setUsernameOrEmail
        loginRequest.setPassword("password123");
    }

    @Test
    void register_shouldReturnUserResponse_whenRegistrationIsSuccessful() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf()) // Add CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void register_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest(); // Empty request
        // Spring Validation should handle this, so no need to mock userService for this specific case of invalid input
        // if the validation is done at controller level with @Valid

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf()) // Add CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Expecting 400 due to validation failures
    }


    @Test
    void login_shouldReturnToken_whenLoginIsSuccessful() throws Exception {
        String token = "test-jwt-token";
        when(userService.login(any(LoginRequest.class))).thenReturn(token);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf()) // Add CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(token)));
    }
    
    // We will add more tests for other endpoints here

    // Added TestConfiguration for providing mocks
    @TestConfiguration
    static class UserControllerTestContextConfiguration {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }
}
