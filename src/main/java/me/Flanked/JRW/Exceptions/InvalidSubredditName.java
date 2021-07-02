package me.Flanked.JRW.Exceptions;

public class InvalidSubredditName  extends RuntimeException {
    public InvalidSubredditName (String errorMessage) {
        super(errorMessage);
    }
}
