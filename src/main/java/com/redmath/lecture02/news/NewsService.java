package com.redmath.lecture02.news;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PreAuthorize("hasAnyRole('REPORTER', 'EDITOR', 'ADMIN')")
    public News create(News news){
        news.setReportedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        news.setNewsId(System.currentTimeMillis());
        news.setReportedAt(LocalDateTime.now());
        return newsRepository.save(news); // TODO: null news objects are not created
    }

    @Transactional
    @PreAuthorize("hasAnyRole('REPORTER', 'EDITOR', 'ADMIN')")
    public News update(Long id, News updatedNews) {
        News currentNews = newsRepository.findById(id)
                .orElseThrow(() -> new NewsNotFoundException(id));

        validateUpdatePermission(currentNews);

        currentNews.setTitle(updatedNews.getTitle());
        currentNews.setDescription(updatedNews.getDescription());
        return newsRepository.save(currentNews);
    }

    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public void delete(Long id) {
        if (newsRepository.existsById(id)) newsRepository.deleteById(id);
        else throw new NewsNotFoundException(id);
    }

    private void validateUpdatePermission(News news){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isOwner = auth.getName().equals(news.getReportedBy());
        boolean isEditor = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role ->
                        role.equals("ROLE_EDITOR") || role.equals("ROLE_ADMIN")
                );

        if (!isOwner && !isEditor) {
            throw new AccessDeniedException(
                    "You are not allowed to update this article.");
        }
    }
}
