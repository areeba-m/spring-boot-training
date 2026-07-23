package com.redmath.lecture02.news;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsResponseDto {
    private Long newsId;
    private String title;
    private String description;
    private String reportedBy;
    private LocalDateTime reportedAt;
}
