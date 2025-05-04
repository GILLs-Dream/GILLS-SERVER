package com.gills_dream.global.apiPayload.exception;

import com.gills_dream.global.apiPayload.code.BaseCode;
import com.gills_dream.global.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private BaseCode code;

    public ReasonDTO getErrorReason() {
        return this.code.getReason();
    }

    public ReasonDTO getErrorReasonHttpStatus(){
        return this.code.getReasonHttpStatus();
    }
}
