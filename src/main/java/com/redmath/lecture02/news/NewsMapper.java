package com.redmath.lecture02.news;

import org.springframework.stereotype.Component;

@Component
public class NewsMapper {
    public NewsResponseDto toResponseDto(News entity) {
        if (entity == null) return null;

        NewsResponseDto dto = new NewsResponseDto();
        dto.setNewsId(entity.getNewsId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setReportedBy(entity.getReportedBy());
        dto.setReportedAt(entity.getReportedAt());
        return dto;
    }
}
