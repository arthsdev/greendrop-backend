package br.com.greendrop.backend.exception.generic;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends BaseException {

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
