package br.com.greendrop.backend.exception.user;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;

public class UserLocationNotFoundException extends BaseException {
    public UserLocationNotFoundException(Object... args) {
        super(ErrorCode.USER_LOCATION_NOT_FOUND, args);
    }
}
