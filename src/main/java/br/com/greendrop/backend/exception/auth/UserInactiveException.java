package br.com.greendrop.backend.exception.auth;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;

public class UserInactiveException extends BaseException {

    public UserInactiveException() {
        super(ErrorCode.USER_INACTIVE);
    }
}
