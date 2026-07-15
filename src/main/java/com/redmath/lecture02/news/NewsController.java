package com.redmath.lecture02.news;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/*
GET /api/v2/news
POST /api/v2/news
GET /api/v2/news/{newsId}
PUT /api/v2/news/{newsId}
DELETE /api/v2/news/{newsId}
 */
@RestController
@RequestMapping("/api/v1/news")
public class NewsController {
    private NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public Map<String, Object> findAll(@RequestParam(required = false, defaultValue = "0") int page,
                                       @RequestParam(required = false, defaultValue = "100") int size){
        Page<News> news = newsService.findAll(page, size);
        return Map.of("content", news.getContent(),
                "page", news.getNumber(),
                "size", news.getSize()
        );

    }

    @GetMapping("/{newsId}")
    public ResponseEntity<News> findOne(@PathVariable Long newsId){
        Optional<News> value = newsService.findOne(newsId);
        if(value.isPresent())
            return ResponseEntity.ok(value.get());
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<News> create(@RequestBody News news){
        return ResponseEntity.status(HttpStatus.CREATED).body(newsService.create(news));
    }

    @PutMapping("/{newsId}")
    public ResponseEntity<News> update(@PathVariable Long newsId,
                       @RequestBody News updatedNews){
        News savedNews = newsService.update(newsId, updatedNews);
        return ResponseEntity.ok(savedNews);
    }

    @DeleteMapping("/{newsId}")
    public ResponseEntity<Void> delete(@PathVariable Long newsId){
        newsService.delete(newsId);
        return ResponseEntity.noContent().build();
    }
}
