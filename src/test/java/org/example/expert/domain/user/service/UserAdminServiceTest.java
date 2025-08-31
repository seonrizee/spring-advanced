package org.example.expert.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
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

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "test@test.com";
    private static final String USER_PASSWORD = "encodedPassword";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    private User userWithUserRole;
    private User userWithAdminRole;
    private UserRoleChangeRequest changeToAdminRequest;
    private UserRoleChangeRequest changeToUserRequest;
    private UserRoleChangeRequest invalidRoleRequest;

    @BeforeEach
    void setUp() {
        userWithUserRole = new User(USER_EMAIL, USER_PASSWORD, UserRole.USER);
        userWithAdminRole = new User(USER_EMAIL, USER_PASSWORD, UserRole.ADMIN);
        changeToAdminRequest = new UserRoleChangeRequest("ADMIN");
        changeToUserRequest = new UserRoleChangeRequest("USER");
        invalidRoleRequest = new UserRoleChangeRequest("INVALID_ROLE");
    }

    @Test
    @DisplayName("USER 역할을 ADMIN 역할로 성공적으로 변경할 수 있다")
    void changeUserRole_userToAdmin_success() {

        // given
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(userWithUserRole));

        // when
        userAdminService.changeUserRole(USER_ID, changeToAdminRequest);

        // then
        assertThat(userWithUserRole.getUserRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository).findById(USER_ID);
    }

    @Test
    @DisplayName("ADMIN 역할을 USER 역할로 성공적으로 변경할 수 있다")
    void changeUserRole_adminToUser_success() {

        // given
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(userWithAdminRole));

        // when
        userAdminService.changeUserRole(USER_ID, changeToUserRequest);

        // then
        assertThat(userWithAdminRole.getUserRole()).isEqualTo(UserRole.USER);
        verify(userRepository).findById(USER_ID);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 역할 변경 시도시 예외가 발생한다")
    void changeUserRole_userNotFound() {

        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> userAdminService.changeUserRole(USER_ID, changeToAdminRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository).findById(USER_ID);
    }

    @Test
    @DisplayName("유효하지 않은 역할로 변경 시도시 예외가 발생한다")
    void changeUserRole_invalidRole() {

        // given
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(userWithUserRole));

        // when, then
        assertThatThrownBy(() -> userAdminService.changeUserRole(USER_ID, invalidRoleRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("유효하지 않은 UserRole");

        // 유효하지 않은 역할이므로 사용자 역할은 변경되지 않아야 함
        assertThat(userWithUserRole.getUserRole()).isEqualTo(UserRole.USER);
        verify(userRepository).findById(USER_ID);
    }
}
