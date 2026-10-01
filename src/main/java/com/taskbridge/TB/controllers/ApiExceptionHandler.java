package com.taskbridge.TB.controllers;

import java.time.Instant;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taskbridge.TB.dto.ApiErrorResponse;

@ControllerAdvice(assignableTypes = {AuditApiController.class, NotificationApiController.class})
public class ApiExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseBody
	public ResponseEntity<ApiErrorResponse> badRequest(IllegalArgumentException exception) {
		return response(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(NoSuchElementException.class)
	@ResponseBody
	public ResponseEntity<ApiErrorResponse> notFound(NoSuchElementException exception) {
		return response(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseBody
	public ResponseEntity<ApiErrorResponse> forbidden(AccessDeniedException exception) {
		return response(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String message) {
		return ResponseEntity.status(status)
				.body(new ApiErrorResponse(status.value(), message, Instant.now()));
	}
}