package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;

    @Transactional
    public void changeUserRole(long userId, UserRoleChangeRequest userRoleChangeRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("사용자를 찾을 수 없습니다."));

        UserRole newRole = UserRole.of(userRoleChangeRequest.getRole());
        UserRole oldRole = user.getUserRole();

        user.updateRole(newRole);

        log.info("사용자 역할 변경 완료 - 사용자 ID: {}, 이전 역할: {}, 새 역할: {}",
                userId, oldRole, newRole);
    }
}
