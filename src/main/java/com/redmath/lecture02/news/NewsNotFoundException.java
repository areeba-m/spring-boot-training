package com.redmath.lecture02.news;

public class NewsNotFoundException extends RuntimeException {
    public NewsNotFoundException(Long id) {
        super("News not found with id:" + id);
    }
}
