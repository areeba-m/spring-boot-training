package com.redmath.lecture02;

import com.redmath.lecture02.news.*;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class NewsApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NewsService newsService;

    private News news1, news2;
    private NewsResponseDto newsResponseDto1, newsResponseDto2;

    @BeforeEach
     void setup(){
        news1 = new News();
        news1.setNewsId(1L);
        news1.setTitle("Test News 1");
        news1.setDescription("Test description 1");
        news1.setReportedBy("Test Reporter");
        news1.setReportedAt(LocalDateTime.of(2026, 7,13, 3, 45));

        news2 = new News();
        news2.setNewsId(2L);
        news2.setTitle("Test News 2");
        news2.setDescription("Test description 2");
        news2.setReportedBy("Test Reporter");
        news2.setReportedAt(LocalDateTime.of(2026, 7,12, 4, 0));

        newsResponseDto1 = new NewsResponseDto();
        newsResponseDto1.setNewsId(news1.getNewsId());
        newsResponseDto1.setTitle(news1.getTitle());
        newsResponseDto1.setDescription(news1.getDescription());
        newsResponseDto1.setReportedBy(news1.getReportedBy());
        newsResponseDto1.setReportedAt(news1.getReportedAt());

        newsResponseDto2 = new NewsResponseDto();
        newsResponseDto2.setNewsId(news2.getNewsId());
        newsResponseDto2.setTitle(news2.getTitle());
        newsResponseDto2.setDescription(news2.getDescription());
        newsResponseDto2.setReportedBy(news2.getReportedBy());
        newsResponseDto2.setReportedAt(news2.getReportedAt());
    }

    /**
     * GET: find all
     * */
    @Test
    @DisplayName("returns default paginated news with default page and size")
    public void findAll_defaultPageAndSize_returnsPage() throws Exception {
        final int defaultPage = 0;
        final int defaultSize = 100;

        List<News> content = List.of(news1, news2);
        Page<News> page = new PageImpl<>(content,
                PageRequest.of(defaultPage, defaultSize),
                content.size()
        );

        when(newsService.findAll(defaultPage,defaultSize)).thenReturn(page);

        mockMvc.perform(get("/api/v1/news")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].newsId").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test News 1"))
                .andExpect(jsonPath("$.content[0].description").value("Test description 1"))
                .andExpect(jsonPath("$.content[1].newsId").value(2));
    }

    @Test
    @DisplayName("returns custom paginated news with specified page and size")
    public void findAll_customPageAndSize_returnsPage() throws Exception {
        final int customPage = 1;
        final int customSize = 5;

        List<News> content = List.of(news1);
        Page<News> page = new PageImpl<>(content,
                PageRequest.of(customPage, customSize),
                6
        );

        when(newsService.findAll(customPage,customSize)).thenReturn(page);

        mockMvc.perform(get("/api/v1/news")
                        .param("page", "1")
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].newsId").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test News 1"))
                .andExpect(jsonPath("$.content[0].description").value("Test description 1"));
    }

    @Test
    @DisplayName("returns empty content when no news exist")
    void findAll_noResults_returnsEmptyContent() throws Exception {
        Page<News> emptyPage = new PageImpl<>(List.of(),
                PageRequest.of(0, 100),
                0
        );

        when(newsService.findAll(anyInt(), anyInt())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    /**
     * GET: find one
     * */
    @Test
    @DisplayName("returns news object found if exists in database")
    void findOne_returnsNews() throws Exception{
        when(newsService.findOne(news1.getNewsId())).thenReturn(newsResponseDto1);

        mockMvc.perform(get("/api/v1/news/{newsId}", news1.getNewsId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.newsId").value(1))
                .andExpect(jsonPath("$.title").value(news1.getTitle()))
                .andExpect(jsonPath("$.description").value(news1.getDescription()))
                .andExpect(jsonPath("$.reportedBy").value(news1.getReportedBy()))
                .andExpect(jsonPath("$.reportedAt").exists());
    }

    @Test
    @DisplayName("returns 404 news object not found if not exists in database")
    void findOne_returnsNotFoundNews() throws Exception{
        Long newsId = 99L;
        when(newsService.findOne(newsId)).thenThrow(new NewsNotFoundException(newsId));

        mockMvc.perform(get("/api/v1/news/{newsId}", newsId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    /**
     * POST: create news
     */
    @Test
    @DisplayName("returns created news object")
    void create_returnsNews() throws Exception{
        when(newsService.create(
                argThat(news -> news.getTitle().equals("Test News 1")))
        ).thenReturn(newsResponseDto1);

        mockMvc.perform(post("/api/v1/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(news1))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                                .attributes(attrs -> attrs.put("sub", "Test Reporter"))
                                .authorities(new SimpleGrantedAuthority("ROLE_REPORTER"))))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.newsId").value(1))
                .andExpect(jsonPath("$.title").value("Test News 1"))
                .andExpect(jsonPath("$.description").value("Test description 1"))
                .andExpect(jsonPath("$.reportedBy").value("Test Reporter"))
                .andExpect(jsonPath("$.reportedAt").exists());
    }

    /***
     * PUT: update news(id, updatedNews)
     */
    @Test
    @DisplayName("returns updated news object that exists in database")
    void update_returnUpdatedNews() throws Exception{
        NewsResponseDto updatedResponseDto = new NewsResponseDto();
        updatedResponseDto.setNewsId(news1.getNewsId());
        updatedResponseDto.setTitle("Updated Test News 1");
        updatedResponseDto.setDescription("Updated Test description 1");
        updatedResponseDto.setReportedBy("Updated Test Reporter");
        updatedResponseDto.setReportedAt(news1.getReportedAt());

        when(newsService.update(
                eq(news1.getNewsId()),
                argThat(news -> news.getTitle().equals("Updated Test News 1")))
        ).thenReturn(updatedResponseDto);

        mockMvc.perform(put("/api/v1/news/{newsId}", news1.getNewsId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedResponseDto))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                                .attributes(attrs -> attrs.put("sub", "Test Reporter"))
                                .authorities(new SimpleGrantedAuthority("ROLE_REPORTER"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.newsId").value(1))
                .andExpect(jsonPath("$.title").value(updatedResponseDto.getTitle()))
                .andExpect(jsonPath("$.description").value(updatedResponseDto.getDescription()))
                .andExpect(jsonPath("$.reportedBy").value(updatedResponseDto.getReportedBy()))
                .andExpect(jsonPath("$.reportedAt").exists());
    }

    /***
     * DELETE: delete news(id)
     */
    @Test
    @DisplayName("returns status 204 No Content when news object deleted that exists in database")
    void delete_returnSuccess() throws Exception{
        doNothing().when(newsService).delete(news1.getNewsId());

        mockMvc.perform(delete("/api/v1/news/{newsId}", news1.getNewsId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                                .attributes(attrs -> attrs.put("sub", "Test Editor"))
                                .authorities(new SimpleGrantedAuthority("ROLE_EDITOR"))))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    @DisplayName("returns nothing, handles exception when news ID does not exist")
    void delete_NotFound_ThrowsException() {
        Long newsId = 99L;
        doThrow(new RuntimeException("Failed to delete. News not found with id:" + newsId))
                .when(newsService).delete(newsId);

        // when runtime exception is thrown,
        // MockMvc catches that unhandled exception and wraps it in a ServletException
        ServletException exception = assertThrows(ServletException.class, () -> {
            mockMvc.perform(delete("/api/v1/news/{newsId}", newsId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                            .attributes(attrs -> attrs.put("sub", "Test Editor"))
                            .authorities(new SimpleGrantedAuthority("ROLE_EDITOR"))));
        });

        assertInstanceOf(RuntimeException.class, exception.getRootCause());
    }
}
