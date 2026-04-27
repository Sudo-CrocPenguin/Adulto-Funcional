package org.adultofuncional.main.shared.exception;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super (message, 403);
    }
    
}
