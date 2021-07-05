package me.flanked.JRW.Exceptions;

public class InvalidSubmissionID extends RuntimeException{
    public InvalidSubmissionID (String errorMessage) {
        super(errorMessage);
    }
}
