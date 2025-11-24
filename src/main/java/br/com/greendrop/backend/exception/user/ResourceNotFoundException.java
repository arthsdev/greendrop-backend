package br.com.greendrop.backend.exception.user;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
