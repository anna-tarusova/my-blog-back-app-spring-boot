package ru.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.CommentRepository;
import ru.practicum.dao.PostRepository;
import ru.practicum.model.Comment;
import ru.practicum.model.Post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Long testPostId;
    private Long testCommentId;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных в БД перед каждым тестом
        Post post = Post.builder()
                .title("Test Post")
                .text("Test Text")
                .likesCount(0)
                .build();
        testPostId = postRepository.save(post).getId();

        Comment comment = Comment.builder()
                .postId(testPostId)
                .text("Test Comment")
                .build();
        testCommentId = commentRepository.save(comment).getId();
    }

    @Test
    void getPost_shouldReturnPostFromDb() throws Exception {
        mockMvc.perform(get("/api/posts/" + testPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPostId.intValue()))
                .andExpect(jsonPath("$.title").value("Test Post"))
                .andExpect(jsonPath("$.tags").isArray());
    }

    @Test
    void getPost_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/posts/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_shouldPersistInDb() throws Exception {
        mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"New Comment\", \"postId\":" + testPostId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("New Comment"));

        // Проверка, что комментарий действительно появился в списке
        mockMvc.perform(get("/api/posts/" + testPostId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.text == 'New Comment')]").exists());
    }

    @Test
    void updateImage_shouldPersistImage() throws Exception {
        byte[] fakeImageBytes = "fake-image-data".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.png", MediaType.IMAGE_PNG_VALUE, fakeImageBytes
        );

        // Тестирование PUT multipart в MockMvc
        mockMvc.perform(multipart("/api/posts/" + testPostId + "/image")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());

        // Проверка, что картинка сохранилась
        mockMvc.perform(get("/api/posts/" + testPostId + "/image"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(fakeImageBytes));
    }

    @Test
    void incrementLikes_shouldIncreaseCount() throws Exception {
        mockMvc.perform(post("/api/posts/" + testPostId + "/likes"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void deleteComment_shouldRemoveFromDb() throws Exception {
        mockMvc.perform(delete("/api/posts/" + testPostId + "/comments/" + testCommentId))
                .andExpect(status().isOk()); // Соответствует ResponseEntity.ok().build() в контроллере

        mockMvc.perform(get("/api/posts/" + testPostId + "/comments/" + testCommentId))
                .andExpect(status().isNotFound());
    }
}