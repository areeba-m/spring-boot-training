package com.redmath.news;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NewsNotFoundException extends RuntimeException {
    public NewsNotFoundException(Long id) {
        super("News not found with id: " + id);
    }
}