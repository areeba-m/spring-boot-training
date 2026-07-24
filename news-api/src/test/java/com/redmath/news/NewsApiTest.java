package com.redmath.news;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static com.redmath.OAuth2TestUsers.user;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NewsApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    ObjectMapper objectMapper;

    private News news;

    @BeforeEach
    void setup() {

        newsRepository.deleteAll();

        news = new News();
        news.setNewsId(1L);
        news.setTitle("Initial title");
        news.setDescription("Initial description");
        news.setReportedBy("reporter");
        news.setReportedAt(LocalDateTime.now());

        newsRepository.save(news);
    }

    @Test
    void shouldReturnAllNews() throws Exception {

        mockMvc.perform(get("/api/v1/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Initial title"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void shouldReturnSingleNews() throws Exception {

        mockMvc.perform(get("/api/v1/news/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Initial title"))
                .andExpect(jsonPath("$.description").value("Initial description"))
                .andExpect(jsonPath("$.reportedBy").value("reporter"));
    }

    @Test
    void shouldReturn404WhenNewsDoesNotExist() throws Exception {

        mockMvc.perform(get("/api/v1/news/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNews() throws Exception {

        NewsCreateDto dto = new NewsCreateDto();
        dto.setTitle("Breaking");
        dto.setDescription("Some description");

        mockMvc.perform(post("/api/v1/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(user("reporter", "ROLE_REPORTER"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Breaking"))
                .andExpect(jsonPath("$.reportedBy").value("reporter"));

        assertEquals(2, newsRepository.count());

        News created = newsRepository.findAll()
                .stream()
                .filter(n -> n.getTitle().equals("Breaking"))
                .findFirst()
                .orElseThrow();

        assertEquals("Some description", created.getDescription());
        assertEquals("reporter", created.getReportedBy());
    }

    @Test
    void shouldRejectCreateWithoutAuthentication() throws Exception {

        NewsCreateDto dto = new NewsCreateDto();
        dto.setTitle("Test");
        dto.setDescription("Desc");

        mockMvc.perform(post("/api/v1/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        assertEquals(1, newsRepository.count());
    }

    @Test
    void ownerShouldUpdateOwnNews() throws Exception {

        NewsCreateDto dto = new NewsCreateDto();
        dto.setTitle("Updated");
        dto.setDescription("Updated description");

        mockMvc.perform(put("/api/v1/news/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(user("reporter", "ROLE_REPORTER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));

        News updated = newsRepository.findById(1L).orElseThrow();

        assertEquals("Updated", updated.getTitle());
        assertEquals("Updated description", updated.getDescription());
    }

    @Test
    void reporterCannotUpdateOthersNews() throws Exception {

        NewsCreateDto dto = new NewsCreateDto();
        dto.setTitle("Illegal");
        dto.setDescription("Illegal");

        mockMvc.perform(put("/api/v1/news/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(user("anotherReporter", "ROLE_REPORTER")))
                .andExpect(status().isForbidden());

        News unchanged = newsRepository.findById(1L).orElseThrow();

        assertEquals("Initial title", unchanged.getTitle());
    }

    @Test
    void editorCanUpdateAnyNews() throws Exception {

        NewsCreateDto dto = new NewsCreateDto();
        dto.setTitle("Editor Updated");
        dto.setDescription("Editor Description");

        mockMvc.perform(put("/api/v1/news/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(user("editor", "ROLE_EDITOR"))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertEquals(
                "Editor Updated",
                newsRepository.findById(1L).orElseThrow().getTitle()
        );
    }

    @Test
    void editorShouldDeleteNews() throws Exception {

        mockMvc.perform(delete("/api/v1/news/1")
                        .with(user("editor", "ROLE_EDITOR"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertFalse(newsRepository.existsById(1L));
    }

    @Test
    void reporterCannotDeleteNews() throws Exception {

        mockMvc.perform(delete("/api/v1/news/1")
                        .with(user("reporter", "ROLE_REPORTER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(newsRepository.existsById(1L));
    }

    @Test
    void deleteNonExistingNewsReturns404() throws Exception {

        mockMvc.perform(delete("/api/v1/news/999")
                        .with(user("editor", "ROLE_EDITOR"))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

}