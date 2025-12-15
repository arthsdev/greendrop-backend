package br.com.greendrop.backend.exception.user;

import br.com.greendrop.backend.exception.global.BaseException;
import br.com.greendrop.backend.exception.global.ErrorCode;

public class ResourceNotFoundException extends BaseException {

    // used in: orElseThrow(ResourceNotFoundException::new)
    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }

    // used to send another error code
    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    // used to send arguments to messages.properties
    public ResourceNotFoundException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
