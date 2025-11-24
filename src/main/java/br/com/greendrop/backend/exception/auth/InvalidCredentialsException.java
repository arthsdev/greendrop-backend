package br.com.greendrop.backend.exception.auth;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
