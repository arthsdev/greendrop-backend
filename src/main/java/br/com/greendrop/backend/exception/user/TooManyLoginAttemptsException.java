package br.com.greendrop.backend.exception.user;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;

public class TooManyLoginAttemptsException extends BaseException {
    public TooManyLoginAttemptsException() {
        super(ErrorCode.USER_TOO_MANY_ATTEMPTS);
    }
}
