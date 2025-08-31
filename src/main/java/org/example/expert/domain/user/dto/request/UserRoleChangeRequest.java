package org.example.expert.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleChangeRequest {

    @NotBlank(message = "사용자 역할은 필수입니다.")
    @Pattern(
            regexp = "^(USER|ADMIN)$",
            message = "사용자 역할은 USER 또는 ADMIN만 가능합니다."
    )
    private String role;
}
