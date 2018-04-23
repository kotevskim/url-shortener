package com.sorsix.martin.urlshortener.domain.exceptions;

public class UrlFormatNotValidException extends RuntimeException {

    public UrlFormatNotValidException() {
    }

    public UrlFormatNotValidException(String url) {
        super(String.format("'%s' is not a valid URL format", url));
    }
}
