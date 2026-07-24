package com.redmath.news;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class News {
    @Id
    private Long newsId;
    private String title;
    private String description;
    private String reportedBy;
    private LocalDateTime reportedAt;
}
