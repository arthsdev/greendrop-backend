package br.com.greendrop.backend.exception.auth;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;

public class MissingTokenException extends BaseException {
    public MissingTokenException() {
        super(ErrorCode.MISSING_TOKEN);
    }
}
