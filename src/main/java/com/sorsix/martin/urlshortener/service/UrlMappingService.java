package com.sorsix.martin.urlshortener.service;

import com.google.common.hash.Hashing;
import com.sorsix.martin.urlshortener.domain.UrlMapping;
import com.sorsix.martin.urlshortener.domain.exceptions.UrlFormatNotValidException;
import com.sorsix.martin.urlshortener.domain.exceptions.UrlMappingNotFoundException;
import com.sorsix.martin.urlshortener.persistence.UrlMappingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class UrlMappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlMappingService.class);

    private final UrlMappingDao urlMappingDao;
    static final String APP_BASE_PATH = "http://localhost:8080/";

    @Autowired
    public UrlMappingService(UrlMappingDao urlMappingDao) {
        this.urlMappingDao = urlMappingDao;
    }

    public UrlMapping createOrGetUrlMapping(String url) {
        if (!isUrlValid(url)) {
            LOGGER.warn("Failed to create URL mapping: '[{}]' is not a valid URL format", url);
            throw new UrlFormatNotValidException(url);
        }
        String shortUrl = shorten(sanitizeURL(url));
        UrlMapping result = urlMappingDao.findById(shortUrl).orElseGet(() -> {
            LOGGER.info("URL mapping created: [{}] -> [{}]", shortUrl, url);
            return urlMappingDao.save(new UrlMapping(url, shortUrl));
        });
        result.setShortUrl(APP_BASE_PATH + result.getShortUrl());
        return result;
    }

    public UrlMapping getUrlMapping(String shortUrl) {
        return urlMappingDao.findById(shortUrl)
                .orElseThrow(() -> new UrlMappingNotFoundException(shortUrl));
    }

    /**
     * Updates the state of the UrlMapping passed as argument:
     * - increments the view count
     * - sets the last accessed date to current date
     *
     * @param urlMapping the UrlMapping object that needs to be updated
     * @return the updater object
     */
    @Transactional
    public UrlMapping touch(UrlMapping urlMapping) {
        urlMapping.setLastAccessed(LocalDateTime.now());
        urlMapping.setViewCount(urlMapping.getViewCount() + 1);
        return urlMapping;
    }

    /**
     * Shortens url using hashing algorithm
     *
     * @param url to be shortened
     * @return the shortened url
     */
    String shorten(String url) {
        return Hashing.murmur3_32()
                .hashString(url, StandardCharsets.UTF_8)
                .toString();
    }

    boolean isUrlValid(String url) {
        LOGGER.info("Validating URL: [{}]", url);
        boolean valid = true;
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            valid = false;
        }
        return valid;
    }

    /**
     * This method should take care of the various issues with a valid url
     * examples:
     * - http:www.google.com
     * - http:/www.google.com
     * - http://www.google.com
     * - https:www.google.com
     * - https:/www.google.com
     * - https://www.google.com
     * - https:www.google.com/
     * - https:/www.google.com/
     * - https://www.google.com/
     * - http:www.google.com/
     * - http:/www.google.com/
     * - http://www.google.com/
     * All the above URL should point to same shortened URL
     *
     * @param url the URL that needs to be sanitized
     */
    String sanitizeURL(String url) {
        if (url.substring(0, 7).equals("http://"))
            url = url.substring(7);

        if (url.substring(0, 6).equals("http:/"))
            url = url.substring(6);

        if (url.substring(0, 5).equals("http:"))
            url = url.substring(5);

        if (url.substring(0, 8).equals("https://"))
            url = url.substring(8);

        if (url.substring(0, 7).equals("https:/"))
            url = url.substring(7);

        if (url.substring(0, 6).equals("https:"))
            url = url.substring(6);

        if (url.charAt(url.length() - 1) == '/')
            url = url.substring(0, url.length() - 1);
        return url;
    }
}
