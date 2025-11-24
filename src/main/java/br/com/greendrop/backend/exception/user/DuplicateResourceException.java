package br.com.greendrop.backend.exception.user;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;

public class DuplicateResourceException extends BaseException {
    public DuplicateResourceException() {
        super(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
    }
}
