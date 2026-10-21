package ru.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.*;
import ru.practicum.service.PostService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    void getPosts_shouldReturnPage() throws Exception {
        PostsPageDto pageDto = new PostsPageDto(List.of(), false, false, 1);
        when(postService.getPostsPage(any(), anyInt(), anyInt())).thenReturn(pageDto);

        mockMvc.perform(get("/api/posts")
                        .param("search", "test")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.lastPage").value(1));
    }

    @Test
    void createPost_shouldReturnCreated() throws Exception {
        PostDto responseDto = new PostDto(1L, "Title", "Text", List.of("tag"), 0L, 0L);
        when(postService.createPost(any(PostCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Title\", \"text\":\"Text\", \"tags\":[\"tag\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateImage_shouldHandleMultipart() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "data".getBytes()
        );
        doNothing().when(postService).updateImage(eq(1L), any(byte[].class));

        // workaround для тестирования PUT multipart в MockMvc
        mockMvc.perform(multipart("/api/posts/1/image")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());

        verify(postService, times(1)).updateImage(eq(1L), any(byte[].class));
    }

    @Test
    void deletePost_shouldReturnNoContent() throws Exception {
        doNothing().when(postService).deletePost(1L);

        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isNoContent());

        verify(postService, times(1)).deletePost(1L);
    }

    @Test
    void createComment_shouldValidatePostIdMatch() throws Exception {
        when(postService.createComment(anyLong(), any(CommentCreateDto.class)))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "postId mismatch"));

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Nice\", \"postId\":2}"))
                .andExpect(status().isBadRequest());
    }
}