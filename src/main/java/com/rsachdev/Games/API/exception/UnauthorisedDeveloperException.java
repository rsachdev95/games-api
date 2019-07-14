package com.rsachdev.Games.API.exception;

public class UnauthorisedDeveloperException extends Exception {

    public UnauthorisedDeveloperException(String message) {
        super(message);
    }

    public UnauthorisedDeveloperException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorisedDeveloperException(Throwable cause) {
        super(cause);
    }

}
