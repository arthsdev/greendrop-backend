package br.com.greendrop.backend.exception.auth;

import br.com.greendrop.backend.exception.global.ErrorCode;
import br.com.greendrop.backend.exception.global.BaseException;

public class UnauthorizedException extends BaseException {

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
