package me.flanked.JRW.Exceptions;

public class InvalidType extends RuntimeException{
    public InvalidType (String errorMessage) {
        super(errorMessage);
    }
}
