package org.example.expert.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_PASSWORD = "test1234";
    private static final String TEST_ROLE = "USER";
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private SigninRequest signinRequest;
    private User savedUser;


    @BeforeEach
    void setUp() {
        // 테스트에서 공통으로 사용할 객체들을 초기화
        signupRequest = new SignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_ROLE);
        signinRequest = new SigninRequest(TEST_EMAIL, TEST_PASSWORD);
        savedUser = new User(TEST_EMAIL, "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(savedUser, "id", 1L);
    }

    @Test
    @DisplayName("회원가입에 성공한다.")
    void signup_success() {

        // given
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        String expectedToken = "test-jwt-token-string";
        given(jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole()))
                .willReturn(expectedToken);

        // when
        SignupResponse response = authService.signup(signupRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getBearerToken()).isEqualTo(expectedToken);

        verify(userRepository, times(1)).existsByEmail(signupRequest.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(signupRequest.getPassword());
        verify(jwtUtil, times(1)).createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole());
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입을 시도하면 예외가 발생한다")
    void signup_failByDuplicateEmail() {

        // given
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

        // when, then
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("이미 존재하는 이메일입니다.");

        verify(userRepository, times(1)).existsByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인에 성공한다.")
    void signin_success() {

        // given
        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(java.util.Optional.of(savedUser));
        given(passwordEncoder.matches(signinRequest.getPassword(), savedUser.getPassword())).willReturn(true);

        String expectedToken = "test-jwt-token-string";
        given(jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole()))
                .willReturn(expectedToken);

        // when
        SigninResponse response = authService.signin(signinRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getBearerToken()).isEqualTo(expectedToken);

        verify(userRepository, times(1)).findByEmail(signinRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(signinRequest.getPassword(), savedUser.getPassword());
        verify(jwtUtil, times(1)).createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인을 시도하면 예외가 발생한다")
    void signin_failByNotFoundEmail() {

        // given
        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(java.util.Optional.empty());

        // when, then
        assertThatThrownBy(() -> authService.signin(signinRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("가입되지 않은 유저입니다.");

        verify(userRepository, times(1)).findByEmail(signinRequest.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).createToken(any(), any(), any());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인을 시도하면 예외가 발생한다")
    void signin_failByInvalidPassword() {

        // given
        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(java.util.Optional.of(savedUser));
        given(passwordEncoder.matches(signinRequest.getPassword(), savedUser.getPassword())).willReturn(false);

        // when, then
        assertThatThrownBy(() -> authService.signin(signinRequest))
                .isInstanceOf(AuthException.class)
                .hasMessage("잘못된 비밀번호입니다.");

        verify(userRepository, times(1)).findByEmail(signinRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(signinRequest.getPassword(), savedUser.getPassword());
        verify(jwtUtil, never()).createToken(any(), any(), any());
    }
}
