package org.adultofuncional.main.shared.exception;

public class NotFoundException extends  BusinessException {
    
    public NotFoundException(String message){
        super(message, 404);
    }
}
