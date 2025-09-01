package org.example.expert.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.config.security.JwtUtil;
import org.example.expert.config.security.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            log.warn("회원가입 실패 - 이미 존재하는 이메일: {}", signupRequest.getEmail());
            throw new InvalidRequestException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        User newUser = new User(
                signupRequest.getEmail(),
                encodedPassword,
                userRole
        );
        User savedUser = userRepository.save(newUser);

        log.info("회원가입 완료 - ID: {}, Email: {}, Role: {}",
                savedUser.getId(), savedUser.getEmail(), userRole);
        String bearerToken = createToken(savedUser);

        return new SignupResponse(bearerToken);
    }

    @Transactional(readOnly = true)
    public SigninResponse signin(SigninRequest signinRequest) {

        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                () -> {
                    log.warn("로그인 실패 - 존재하지 않는 이메일: {}", signinRequest.getEmail());
                    return new AuthException("이메일 또는 비밀번호가 올바르지 않습니다.");
                });

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            log.warn("로그인 실패 - 잘못된 비밀번호, 사용자 ID: {}, Email: {}",
                    user.getId(), user.getEmail());
            throw new AuthException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        log.info("로그인 성공 - 사용자 ID: {}, Email: {}", user.getId(), user.getEmail());

        String bearerToken = createToken(user);
        return new SigninResponse(bearerToken);
    }

    private String createToken(User user) {
        return jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
    }
}
