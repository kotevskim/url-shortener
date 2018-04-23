package com.sorsix.martin.urlshortener.domain.exceptions;

public class UrlMappingNotFoundException extends RuntimeException {

    public UrlMappingNotFoundException() {
    }

    public UrlMappingNotFoundException(String url) {
        super(String.format("'%s' is not mapped to an existing URL", url));
    }


}
