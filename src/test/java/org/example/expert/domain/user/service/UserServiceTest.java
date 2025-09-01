package org.example.expert.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.example.expert.config.security.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "test@test.com";
    private static final String OLD_PASSWORD = "oldPassword";
    private static final String NEW_PASSWORD = "newPassword";
    private static final String ENCODED_OLD_PASSWORD = "encodedOldPassword";
    private static final String ENCODED_NEW_PASSWORD = "encodedNewPassword";
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;
    private User user;
    private UserChangePasswordRequest passwordRequest;

    @BeforeEach
    void setUp() {
        user = new User(USER_EMAIL, ENCODED_OLD_PASSWORD, null);
        passwordRequest = new UserChangePasswordRequest(OLD_PASSWORD, NEW_PASSWORD);
    }

    @Test
    @DisplayName("userId를 이용하여 유저 정보를 조회할 수 있다")
    void getUser_success() {

        // given
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUser(USER_ID);

        // then
        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        verify(userRepository).findById(USER_ID);
    }

    @Test
    @DisplayName("존재하지 않는 userId로 조회시 예외가 발생한다")
    void getUser_userNotFound() {

        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> userService.getUser(USER_ID))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("유저는 비밀번호를 변경할 수 있다")
    void changePassword_success() {

        // given
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(OLD_PASSWORD, user.getPassword())).willReturn(true);
        given(passwordEncoder.matches(NEW_PASSWORD, user.getPassword())).willReturn(false);
        given(passwordEncoder.encode(NEW_PASSWORD)).willReturn(ENCODED_NEW_PASSWORD);

        // when
        userService.changePassword(USER_ID, passwordRequest);

        // then
        assertThat(user.getPassword()).isEqualTo(ENCODED_NEW_PASSWORD);
        verify(userRepository).findById(USER_ID);
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD);
        verify(passwordEncoder).matches(NEW_PASSWORD, ENCODED_OLD_PASSWORD);
        verify(passwordEncoder).encode(NEW_PASSWORD);
    }

    @Test
    @DisplayName("존재하지 않는 유저의 비밀번호 변경 시도시 예외가 발생한다")
    void changePassword_userNotFound() {

        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> userService.changePassword(USER_ID, passwordRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("잘못된 기존 비밀번호를 입력하면 예외가 발생한다")
    void changePassword_wrongOldPassword() {

        // given
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(OLD_PASSWORD, user.getPassword())).willReturn(false); // 기존 비밀번호 불일치

        // when, then
        assertThatThrownBy(() -> userService.changePassword(USER_ID, passwordRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("잘못된 비밀번호입니다.");

        // 기존 비밀번호가 틀렸으므로 새 비밀번호 검증과 인코딩은 실행되지 않아야 함
        verify(passwordEncoder).matches(OLD_PASSWORD, user.getPassword());
        verify(passwordEncoder, never()).matches(NEW_PASSWORD, user.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("새 비밀번호가 기존 비밀번호와 같으면 예외가 발생한다")
    void changePassword_samePassword() {

        // given
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(OLD_PASSWORD, user.getPassword())).willReturn(true); // 기존 비밀번호 일치
        given(passwordEncoder.matches(NEW_PASSWORD, user.getPassword())).willReturn(true); // 새 비밀번호가 기존과 동일

        // when, then
        assertThatThrownBy(() -> userService.changePassword(USER_ID, passwordRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");

        // 기존 비밀번호 확인 후 새 비밀번호 중복 검사까지만 실행되고 인코딩은 실행되지 않아야 함
        verify(passwordEncoder).matches(OLD_PASSWORD, user.getPassword());
        verify(passwordEncoder).matches(NEW_PASSWORD, user.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }
}