package com.sorsix.martin.urlshortener.service;

import com.google.common.hash.Hashing;
import com.sorsix.martin.urlshortener.domain.UrlMapping;
import com.sorsix.martin.urlshortener.domain.exceptions.UrlFormatNotValidException;
import com.sorsix.martin.urlshortener.domain.exceptions.UrlMappingNotFoundException;
import com.sorsix.martin.urlshortener.persistence.UrlMappingDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.sorsix.martin.urlshortener.service.UrlMappingService.APP_BASE_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class UrlMappingServiceTest {

    @Mock
    private UrlMappingDao urlMappingDao;
    @InjectMocks
    private UrlMappingService urlMappingService;

    @Before
    public void setUp() {
        this.urlMappingService = new UrlMappingService(urlMappingDao);
    }

    @Test
    public void test_create_or_get_url_mapping_for_non_existing_mapping() {
        String shortUrl = "a1b2c3";
        String originalUrl = "https://www.imdb.com";

        when(urlMappingDao.findById(any(String.class))).thenReturn(Optional.empty());
        when(urlMappingDao.save(any(UrlMapping.class))).thenReturn(new UrlMapping(originalUrl, shortUrl));

        UrlMapping actual = urlMappingService.createOrGetUrlMapping(originalUrl);
        assertEquals(APP_BASE_PATH + shortUrl, actual.getShortUrl());
        assertEquals(originalUrl, actual.getOriginalUrl());
        assertEquals(Long.valueOf(0L), actual.getViewCount());
        assertEquals(null, actual.getLastAccessed());
    }

    @Test
    public void test_create_or_get_url_mapping_for_existing_mapping() {
        String shortUrl = "a1b2c3";
        String originalUrl = "https://www.imdb.com";

        when(urlMappingDao.findById(any(String.class)))
                .thenReturn(Optional.of(new UrlMapping(originalUrl, shortUrl)));

        UrlMapping actual = urlMappingService.createOrGetUrlMapping(originalUrl);
        assertEquals(APP_BASE_PATH + shortUrl, actual.getShortUrl());
        assertEquals(originalUrl, actual.getOriginalUrl());
        assertEquals(Long.valueOf(0L), actual.getViewCount());
        assertEquals(null, actual.getLastAccessed());
    }

    @Test(expected = UrlFormatNotValidException.class)
    public void test_create_or_get_url_mapping_throws_exception_for_invalid_url_format() {
        String url = "htt://www.imdb.com";
        urlMappingService.createOrGetUrlMapping(url);
    }

    @Test
    public void test_get_url_mapping() {
        String shortUrl = "1a2b3c";
        String originalUrl = "https://www.imdb.com";
        when(urlMappingDao.findById(shortUrl))
                .thenReturn(Optional.of(new UrlMapping(originalUrl, shortUrl)));
        UrlMapping actual = urlMappingService.getUrlMapping(shortUrl);
        assertNotNull(actual);
        assertEquals(shortUrl, actual.getShortUrl());
        assertEquals(originalUrl, actual.getOriginalUrl());
        assertEquals(Long.valueOf(0L), actual.getViewCount());
        assertEquals(null, actual.getLastAccessed());
    }

    @Test(expected = UrlMappingNotFoundException.class)
    public void test_get_url_mapping_throws_exception_for_non_existing_url_mapping() {
        String shortUrl = "1a2b3c";
        when(urlMappingDao.findById(shortUrl)).thenThrow(new UrlMappingNotFoundException());
        urlMappingService.getUrlMapping(shortUrl);
    }

    @Test
    public void test_touch() {
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setViewCount(0L);
        urlMappingService.touch(urlMapping);
        assertEquals(Long.valueOf(1L), urlMapping.getViewCount());
        assertTrue((Duration.between(LocalDateTime.now(), urlMapping.getLastAccessed()).getSeconds() < 1));
    }

    @Test
    public void test_shorten() {
        String url = "http://www.google.com";
        String expected = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
        String actual = urlMappingService.shorten(url);
        assertEquals(expected, actual);
    }

    @Test
    public void test_is_url_valid_true_for_valid_http_url() {
        String url = "http://www.imdb.com";
        boolean actual = urlMappingService.isUrlValid(url);
        assertEquals(true, actual);
    }

    @Test
    public void test_is_url_valid_true_for_valid_https_url() {
        String url = "https://www.imdb.com";
        boolean actual = urlMappingService.isUrlValid(url);
        assertEquals(true, actual);
    }

    @Test
    public void test_is_url_valid_true_for_valid_url_with_no_slash() {
        String url = "http:imdb.com";
        boolean actual = urlMappingService.isUrlValid(url);
        assertEquals(true, actual);
    }

    @Test
    public void test_is_url_valid_false_for_invalid_url_format_1() {
        String url = "www.imdb.com";
        boolean actual = urlMappingService.isUrlValid(url);
        assertEquals(false, actual);
    }

    @Test
    public void test_is_url_valid_false_for_invalid_url_format_2() {
        String url = "htt://www.imdb.com";
        boolean actual = urlMappingService.isUrlValid(url);
        assertEquals(false, actual);
    }

    @Test
    public void test_is_url_valid_false_for_invalid_url_format_3() {
        String url = "httpss://google.com";
        boolean actual = urlMappingService.isUrlValid(url);
        assertEquals(false, actual);
    }

    @Test
    public void test_sanitize_url_for_http_without_slashes() {
        String url = "http:www.imdb.com";
        String actual = urlMappingService.sanitizeURL(url);
        String expected = "www.imdb.com";
        assertEquals(expected, actual);
    }

    @Test
    public void test_sanitize_url_for_http_with_one_slash() {
        String url = "http:/www.imdb.com";
        String actual = urlMappingService.sanitizeURL(url);
        String expected = "www.imdb.com";
        assertEquals(expected, actual);
    }

    @Test
    public void test_sanitize_url_for_http_with_two_slashes() {
        String url = "http://www.imdb.com";
        String actual = urlMappingService.sanitizeURL(url);
        String expected = "www.imdb.com";
        assertEquals(expected, actual);
    }

    @Test
    public void test_sanitize_url_for_https_without_slashes() {
        String url = "https:www.imdb.com";
        String actual = urlMappingService.sanitizeURL(url);
        String expected = "www.imdb.com";
        assertEquals(expected, actual);
    }

    @Test
    public void test_sanitize_url_for_https_with_one_slash() {
        String url = "https:/www.imdb.com";
        String actual = urlMappingService.sanitizeURL(url);
        String expected = "www.imdb.com";
        assertEquals(expected, actual);
    }

    @Test
    public void test_sanitize_url_for_https_with_two_slashes() {
        String url = "https://www.imdb.com";
        String actual = urlMappingService.sanitizeURL(url);
        String expected = "www.imdb.com";
        assertEquals(expected, actual);
    }

    @Test
    public void test_sanitize_url_for_ending_slash() {
        String url = "https://www.imdb.com/";
        String actual = urlMappingService.sanitizeURL(url);
        String expected = "www.imdb.com";
        assertEquals(expected, actual);
    }
}