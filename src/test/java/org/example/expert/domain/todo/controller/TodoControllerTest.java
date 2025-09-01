package org.example.expert.domain.todo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.example.expert.config.security.FilterConfig;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = TodoController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = FilterConfig.class)
        }
)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
    }

    @Test
    @DisplayName("할 일 생성 성공")
    void saveTodo_success() throws Exception {
        // given
        TodoSaveRequest request = new TodoSaveRequest("Test Title", "Test Contents");
        UserResponse userResponse = new UserResponse(authUser.getId(), authUser.getEmail());
        TodoSaveResponse response = new TodoSaveResponse(1L, "Test Title", "Test Contents", "맑음", userResponse);

        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", authUser.getId())
                        .requestAttr("userRole", authUser.getUserRole().name())
                        .requestAttr("email", authUser.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    @DisplayName("할 일 목록 조회 성공")
    void getTodos_success() throws Exception {
        // given
        UserResponse userResponse = new UserResponse(authUser.getId(), authUser.getEmail());
        TodoResponse todoResponse = new TodoResponse(1L, "Test Title", "Test Contents", "맑음", userResponse, null, null);
        Page<TodoResponse> responsePage = new PageImpl<>(Collections.singletonList(todoResponse));

        given(todoService.getTodos(anyInt(), anyInt())).willReturn(responsePage);

        // when & then
        mockMvc.perform(get("/todos")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Test Title"));
    }

    @Test
    @DisplayName("할 일 단건 조회 성공")
    void getTodo_success() throws Exception {
        // given
        long todoId = 1L;
        UserResponse userResponse = new UserResponse(authUser.getId(), authUser.getEmail());
        TodoResponse response = new TodoResponse(todoId, "Test Title", "Test Contents", "맑음", userResponse, null, null);

        given(todoService.getTodo(anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/todos/{todoId}", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todoId))
                .andExpect(jsonPath("$.title").value("Test Title"));
    }
}
