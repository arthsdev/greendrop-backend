package br.com.greendrop.backend.exception.generic;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;

public class BadRequestException extends BaseException {

    public BadRequestException() {
        super(ErrorCode.BAD_REQUEST);
    }

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BadRequestException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
