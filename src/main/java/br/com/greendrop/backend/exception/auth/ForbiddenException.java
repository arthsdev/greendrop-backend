package br.com.greendrop.backend.exception.auth;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;

public class ForbiddenException extends BaseException {
    public ForbiddenException(ErrorCode errorCode, Object... args) { super(errorCode, args); }
    public ForbiddenException(String message) { super(ErrorCode.UNAUTHORIZED, message); }
}