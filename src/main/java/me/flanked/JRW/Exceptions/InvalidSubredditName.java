package me.flanked.JRW.Exceptions;

public class InvalidSubredditName  extends RuntimeException {
    public InvalidSubredditName (String errorMessage) {
        super(errorMessage);
    }
}
