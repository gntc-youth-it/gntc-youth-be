package com.gntcyouthbe.common.exception.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.UserNotFoundException;
import com.gntcyouthbe.common.exception.model.ExceptionCode;
import com.gntcyouthbe.common.exception.model.ExceptionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("AuthorizationDeniedException 발생 시 401을 반환한다")
    void handleAuthorizationDeniedException() {
        // given
        AuthorizationDeniedException exception = new AuthorizationDeniedException("Access denied");

        // when
        ResponseEntity<ExceptionResponse> response =
                globalExceptionHandler.handleAuthorizationDeniedException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ExceptionCode.UNAUTHORIZED.getCode());
        assertThat(response.getBody().message()).isEqualTo(ExceptionCode.UNAUTHORIZED.getMessage());
    }

    @Test
    @DisplayName("UserNotFoundException 발생 시 404를 반환한다")
    void handleUserNotFoundException() {
        // given
        UserNotFoundException exception = new UserNotFoundException(ExceptionCode.USER_NOT_FOUND);

        // when
        ResponseEntity<ExceptionResponse> response =
                globalExceptionHandler.handleUserNotFoundException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ExceptionCode.USER_NOT_FOUND.getCode());
        assertThat(response.getBody().message()).isEqualTo(ExceptionCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("EntityNotFoundException 발생 시 404를 반환한다")
    void handleEntityNotFoundException() {
        // given
        EntityNotFoundException exception = new EntityNotFoundException(ExceptionCode.BOOK_NOT_FOUND);

        // when
        ResponseEntity<ExceptionResponse> response =
                globalExceptionHandler.handleEntityNotFoundException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ExceptionCode.BOOK_NOT_FOUND.getCode());
        assertThat(response.getBody().message()).isEqualTo(ExceptionCode.BOOK_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("일반 Exception 발생 시 500을 반환한다")
    void handleException() {
        // given
        Exception exception = new RuntimeException("unexpected error");

        // when
        ResponseEntity<ExceptionResponse> response =
                globalExceptionHandler.handleException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ExceptionCode.INTERNAL_SERVER_ERROR.getCode());
        assertThat(response.getBody().message()).isEqualTo(ExceptionCode.INTERNAL_SERVER_ERROR.getMessage());
    }
}
