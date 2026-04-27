package org.adultofuncional.main.shared.exception;

public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {

        super(message, 401);
    }
}
