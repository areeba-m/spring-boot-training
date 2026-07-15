package com.redmath.lecture02.news;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class NewsService {
    private NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public Page<News> findAll(int page, int size){
        if (page<0) page=0;
        if (size<0 || size>100) size=100;
        return newsRepository.findAll(PageRequest.of(page,size));
    }

    public Optional<News> findOne(Long id){
        return newsRepository.findById(id);
    }

    public News create(News news){
        news.setNewsId(System.currentTimeMillis());
        news.setReportedAt(LocalDateTime.now());
        return newsRepository.save(news); // TODO: null news objects are not created
    }

    @Transactional
    public News update(Long id, News updatedNews){
        return newsRepository.findById(id)
                .map(currentNews -> {
                    currentNews.setTitle(updatedNews.getTitle());
                    currentNews.setDescription(updatedNews.getDescription());
                    currentNews.setReportedBy(updatedNews.getReportedBy());

                    return newsRepository.save(currentNews);

                }).orElseThrow(() -> new RuntimeException("Failed to update. News not found with id: " + id));
    }

    public void delete(Long id){
        if (newsRepository.existsById(id)) newsRepository.deleteById(id);
        else throw new RuntimeException("Failed to delete. News not found with id:" +id);
    }
}
