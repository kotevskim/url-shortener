package com.sorsix.martin.urlshortener.api;

import com.sorsix.martin.urlshortener.domain.exceptions.UrlMappingNotFoundException;
import com.sorsix.martin.urlshortener.domain.exceptions.UrlFormatNotValidException;
import org.springframework.hateoas.VndErrors.VndError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class UrlControllerAdvice {

    @ExceptionHandler(UrlMappingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    VndError urlMappingNotFoundExceptionHandler(UrlMappingNotFoundException ex) {
        return new VndError("error", ex.getMessage());
    }

    @ExceptionHandler(UrlFormatNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    VndError urlNotValidExceptionHandler(UrlFormatNotValidException ex) {
        return new VndError("error", ex.getMessage());
    }
}
