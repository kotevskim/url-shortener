package com.sorsix.martin.urlshortener.api;

import com.sorsix.martin.urlshortener.domain.UrlMapping;
import com.sorsix.martin.urlshortener.service.UrlMappingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
@RestController
public class UrlController {

    private final UrlMappingService urlMappingService;

    public UrlController(UrlMappingService urlMappingService) {
        this.urlMappingService = urlMappingService;
    }

    @GetMapping("new/**")
    public ResponseEntity<UrlMapping> newUrlMapping(HttpServletRequest request) {
        String url = getUrlFromPath(request);
        UrlMapping urlMapping = this.urlMappingService.createOrGetUrlMapping(url);
        return ResponseEntity.status(HttpStatus.CREATED).body(urlMapping);
    }

    @GetMapping("{shortUrl}")
    public RedirectView redirectToOriginalUrl(@PathVariable String shortUrl) {
        UrlMapping urlMapping = urlMappingService.getUrlMapping(shortUrl);
        urlMappingService.touch(urlMapping);
        return new RedirectView(urlMapping.getOriginalUrl());
    }

    private String getUrlFromPath(HttpServletRequest request) {
        final String path =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern =
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        return new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, path);
    }
}
