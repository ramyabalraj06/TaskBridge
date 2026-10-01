package com.taskbridge.TB.dto;

import java.time.Instant;

public record ApiErrorResponse(int status, String error, Instant timestamp) {
}