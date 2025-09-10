package com.gills_dream.global.apiPayload.code.status;

import com.gills_dream.global.apiPayload.code.BaseCode;
import com.gills_dream.global.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode {

    // 기본 에러 응답
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 로그인 관련 에러 응답
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "WRONG PASSWORD", "비밀번호 입력이 틀렸습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID TOKEN", "토큰이 유효하지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED TOKEN", "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "UNSUPPORTED TOKEN", "지원하지 않는 토큰입니다."),
    REDIS_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "REDIS KEY NOT FOUND", "요청한 키가 Redis에 존재하지 않습니다."),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "INVALID SIGNATURE", "잘못된 JWT 서명입니다"),
    CANNOT_LOAD_USER_FROM_KAKAO(HttpStatus.NOT_FOUND, "CANNOT LOAD USER FROM KAKAO", "Kakao에서 해당 유저를 불러오지 못했습니다."),
    EMAIL_IS_REQUIRED(HttpStatus.BAD_REQUEST, "EMAIL IS REQUIRED", "이메일은 필수입니다."),

    // 멤버 관련 에러 응답
    MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "USER ALREADY EXISTS", "유저가 이미 존재합니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER NOT FOUND", "유저를 찾을 수 없습니다."),
    NICKNAME_IS_NECESSARY(HttpStatus.BAD_REQUEST, "NICKNAME IS NECESSARY", "닉네임은 필수입니다."),
    DATE_INDEX_MISMATCH(HttpStatus.BAD_REQUEST, "DATE INDEX MISMATCH", "날짜와 인덱스 모드가 일치하지 않습니다."),

    // 플랜 관련 에러 응답
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAN NOT FOUND", "계획을 찾을 수 없습니다."),
    TRANSPORT_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "TRANSPORT TYPE NOT FOUND", "해당 교통수단이 존재하지 않습니다."),
    INVALID_ROUTE_REQUEST(HttpStatus.BAD_REQUEST, "INVALID ROUTE REQUEST", "잘못된 경로 요청입니다."),
    PLACE_COUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PLACE COUNT MISMATCH", "유효한 장소의 개수가 부족합니다."),
    PLACE_CONTENT_MISMATCH(HttpStatus.BAD_REQUEST, "PLAN CONTENT MISMATCH", "플랜의 내용이 올바르지 않습니다."),
    PLAN_NOT_GENERATED(HttpStatus.BAD_REQUEST, "PLAN NOT GENERATED", "여행이 생성되지 않았습니다."),
    PLAN_SUMMARY_NOT_GENERATED(HttpStatus.BAD_REQUEST, "PLAN SUMMARY NOT GENERATED", "여행 요약이 생성되지 않았습니다."),

    // Enum 관련 에러 응답
    NO_SUCH_CATEGORY(HttpStatus.NOT_FOUND, "NO SUCH CATEGORY", "해당 카테고리가 존재하지 않습니다."),
    NO_SUCH_REGION(HttpStatus.NOT_FOUND, "NO SUCH REGION", "해당 지역이 존재하지 않습니다."),
    NO_SUCH_PLACE_TYPE(HttpStatus.NOT_FOUND, "NO SUCH PLACE TYPE", "해당 장소 타입이 존재하지 않습니다." ),
    TRANSPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "TRANSPORT NOT FOUND", "교통수단이 선택되지 않았습니다." ),
    NO_SUCH_MAP(HttpStatus.NOT_FOUND, "NO SUCH MAP", "해당 맵이 존재하지 않습니다."),

    // 파이썬 서버 관련 에러 응답
    PYTHON_SERVER_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PYTHON SERVER CONNECTION ERROR", "파이썬 서버에 연결할 수 없습니다."),
    CANNOT_PROCESS_VIDEO(HttpStatus.BAD_REQUEST, "CANNOT PROCESS VIDEO", "비디오 프로세스중 에러가 발생했습니다."),

    // 장소 관련 에러 응답
    CANNOT_FIND_PLACE_FROM_REDIS(HttpStatus.NOT_FOUND, "CANNOT FIND PLACE FROM REDIS", "레디스에서 해당 장소를 찾을 수 없습니다."),
    CANNOT_FIND_PLACE_FROM_DB(HttpStatus.NOT_FOUND, "CANNOT FIND PLACE FROM DB", "디비에서 해당 장소를 찾을 수 없습니다."),

    // s3 관련 에러 응답
    CANNOT_PROCESS_EXTENSION(HttpStatus.BAD_REQUEST, "CANNOT PROCESS EXTENSION", "해당 확장자를 찾을 수 없습니다."),
    CANNOT_FIND_IMAGE(HttpStatus.BAD_REQUEST, "CANNOT FIND IMAGE", "해당 이미지를 s3에서 찾을 수 없습니다."),
    CANNOT_UPLOAD_IMAGE(HttpStatus.BAD_REQUEST, "CANNOT UPLOAD IMAGE", "이미지를 s3에 업로드할 수 없습니다."),
    CANNOT_DELETE_IMAGE(HttpStatus.BAD_REQUEST, "CANNOT DELETE IMAGE", "이미지를 s3에서 삭제할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
