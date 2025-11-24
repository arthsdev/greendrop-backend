package br.com.greendrop.backend.exception.user;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;

public class PasswordInvalidException extends BaseException {

    public PasswordInvalidException() {
        super(ErrorCode.PASSWORD_INVALID);
    }
}
