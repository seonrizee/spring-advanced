package org.example.expert.domain.manager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.example.expert.config.security.FilterConfig;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = ManagerController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = FilterConfig.class)
        }
)
class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ManagerService managerService;

    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
    }

    @Test
    @DisplayName("담당자 지정 성공")
    void saveManager_success() throws Exception {

        // given
        long todoId = 1L;
        long managerUserId = 2L;
        ManagerSaveRequest request = new ManagerSaveRequest(managerUserId);
        UserResponse managerUserResponse = new UserResponse(managerUserId, "manager@test.com");
        ManagerSaveResponse response = new ManagerSaveResponse(1L, managerUserResponse);

        given(managerService.saveManager(any(AuthUser.class), anyLong(), any(ManagerSaveRequest.class))).willReturn(
                response);

        // when & then
        mockMvc.perform(post("/todos/{todoId}/managers", todoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", authUser.getId())
                        .requestAttr("userRole", authUser.getUserRole().name())
                        .requestAttr("email", authUser.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.user.id").value(managerUserId));
    }

    @Test
    @DisplayName("담당자 목록 조회 성공")
    void getManagers_success() throws Exception {

        // given
        long todoId = 1L;
        UserResponse managerUserResponse = new UserResponse(2L, "manager@test.com");
        ManagerResponse managerResponse = new ManagerResponse(1L, managerUserResponse);
        List<ManagerResponse> responseList = Collections.singletonList(managerResponse);

        given(managerService.getManagers(anyLong())).willReturn(responseList);

        // when & then
        mockMvc.perform(get("/todos/{todoId}/managers", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].user.id").value(2L));
    }

    @Test
    @DisplayName("담당자 삭제 성공")
    void deleteManager_success() throws Exception {

        // given
        long todoId = 1L;
        long managerId = 1L;
        doNothing().when(managerService).deleteManager(anyLong(), anyLong(), anyLong());

        // when & then
        mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todoId, managerId)
                        .requestAttr("userId", authUser.getId())
                        .requestAttr("userRole", authUser.getUserRole().name())
                        .requestAttr("email", authUser.getEmail()))
                .andExpect(status().isNoContent());
    }
}
