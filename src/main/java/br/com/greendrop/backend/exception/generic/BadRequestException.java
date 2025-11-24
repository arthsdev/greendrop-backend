package br.com.greendrop.backend.exception.generic;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;

public class BadRequestException extends BaseException {
    public BadRequestException(Object... args) {
        super(ErrorCode.BAD_REQUEST, args);
    }
}