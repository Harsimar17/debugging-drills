package com.acmelogistics.dispatch.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse() {
    }

    private ErrorResponse(Builder builder) {
        this.status = builder.status;
        this.error = builder.error;
        this.message = builder.message;
        this.timestamp = builder.timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static final class Builder {
        private int status;
        private String error;
        private String message;
        private LocalDateTime timestamp;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}
