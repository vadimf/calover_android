package com.varteq.catslovers.api.entity;

public class ErrorResponse {

    private String message;
    private Integer code;

    public ErrorResponse(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code != null ? code : 0;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}