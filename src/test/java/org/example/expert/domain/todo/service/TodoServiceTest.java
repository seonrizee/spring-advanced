package org.example.expert.domain.todo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Optional;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "test@test.com";
    private static final Long TODO_ID = 1L;
    private static final String TODO_TITLE = "Test Title";
    private static final String TODO_CONTENTS = "Test Contents";
    private static final String WEATHER_INFO = "맑음";

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    private AuthUser authUser;
    private User user;
    private TodoSaveRequest todoSaveRequest;
    private Todo todo;

    @BeforeEach
    void setUp() {
        authUser = new AuthUser(USER_ID, USER_EMAIL, UserRole.USER);
        user = User.fromAuthUser(authUser);
        todoSaveRequest = new TodoSaveRequest(TODO_TITLE, TODO_CONTENTS);
        todo = new Todo(TODO_TITLE, TODO_CONTENTS, WEATHER_INFO, user);
        ReflectionTestUtils.setField(todo, "id", TODO_ID);
    }

    @Test
    @DisplayName("Todo 저장 성공")
    void saveTodo_success() {
        // given
        given(weatherClient.getTodayWeather()).willReturn(WEATHER_INFO);
        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        // when
        TodoSaveResponse response = todoService.saveTodo(authUser, todoSaveRequest);

        // then
        assertThat(response.getId()).isEqualTo(TODO_ID);
        assertThat(response.getTitle()).isEqualTo(TODO_TITLE);
        assertThat(response.getContents()).isEqualTo(TODO_CONTENTS);
        assertThat(response.getWeather()).isEqualTo(WEATHER_INFO);
        assertThat(response.getUser().getId()).isEqualTo(USER_ID);

        verify(weatherClient, times(1)).getTodayWeather();
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Todo 목록 조회 성공")
    void getTodos_success() {
        // given
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Todo> todoPage = new PageImpl<>(Collections.singletonList(todo), pageable, 1);
        given(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).willReturn(todoPage);

        // when
        Page<TodoResponse> response = todoService.getTodos(page, size);

        // then
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(TODO_ID);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo(TODO_TITLE);

        verify(todoRepository, times(1)).findAllByOrderByModifiedAtDesc(pageable);
    }

    @Test
    @DisplayName("Todo 단건 조회 성공")
    void getTodo_success() {
        // given
        given(todoRepository.findByIdWithUser(TODO_ID)).willReturn(Optional.of(todo));

        // when
        TodoResponse response = todoService.getTodo(TODO_ID);

        // then
        assertThat(response.getId()).isEqualTo(TODO_ID);
        assertThat(response.getTitle()).isEqualTo(TODO_TITLE);
        assertThat(response.getUser().getId()).isEqualTo(USER_ID);

        verify(todoRepository, times(1)).findByIdWithUser(TODO_ID);
    }

    @Test
    @DisplayName("존재하지 않는 Todo 조회 시 예외 발생")
    void getTodo_notFound() {
        // given
        given(todoRepository.findByIdWithUser(TODO_ID)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> todoService.getTodo(TODO_ID))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("Todo not found");

        verify(todoRepository, times(1)).findByIdWithUser(TODO_ID);
    }
}