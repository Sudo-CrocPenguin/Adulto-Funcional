package org.adultofuncional.main.shared.exception;

public class ConflictException extends BusinessException {
    
    public ConflictException(String message) {

        super(message, 409);
    }
}
