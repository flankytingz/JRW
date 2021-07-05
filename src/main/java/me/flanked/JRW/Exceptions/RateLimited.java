package me.flanked.JRW.Exceptions;

public class RateLimited extends RuntimeException{
    public RateLimited (String errorMessage) {
        super(errorMessage);
    }
}
