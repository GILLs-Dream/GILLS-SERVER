package com.gills_dream.global.apiPayload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.gills_dream.global.apiPayload.code.BaseCode;
import com.gills_dream.global.apiPayload.code.ReasonDTO;
import com.gills_dream.global.apiPayload.code.status.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;
    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    public ApiResponse(ReasonDTO reason, T result) {
        this.isSuccess = reason.getIsSuccess();
        this.code = reason.getCode();
        this.message = reason.getMessage();
        this.result = result;
    }


    // 성공한 경우 응답 생성
    public static <T> ApiResponse<T> onSuccess(T result){
        return new ApiResponse<>(true, SuccessStatus._OK.getCode() , SuccessStatus._OK.getMessage(), result);
    }

    public static <T> ApiResponse<T> onSuccess(SuccessStatus status) {
        return new ApiResponse<>(status.getReason(), null);
    }

    public static <T> ApiResponse<T> onSuccess(SuccessStatus status, T result) {
        return new ApiResponse<>(status.getReason(), result);
    }

    public static <T> ApiResponse<T> of(BaseCode code, T result){
        return new ApiResponse<>(true, code.getReasonHttpStatus().getCode() , code.getReasonHttpStatus().getMessage(), result);
    }


    // 실패한 경우 응답 생성
    public static <T> ApiResponse<T> onFailure(String code, String message, T data){
        return new ApiResponse<>(false, code, message, data);
    }
}

