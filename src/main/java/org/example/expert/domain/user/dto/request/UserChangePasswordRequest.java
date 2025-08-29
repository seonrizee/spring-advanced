package org.example.expert.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordRequest {

    @NotBlank
    private String oldPassword;

    @NotBlank
    @Length(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[A-Z]).*$", message = "새 비밀번호는 숫자와 대문자를 포함해야 합니다.")
    private String newPassword;
}
