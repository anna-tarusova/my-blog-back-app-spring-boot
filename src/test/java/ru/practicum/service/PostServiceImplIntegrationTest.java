package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.PostRepository;
import ru.practicum.repository.TagRepository;
import ru.practicum.dto.CommentCreateDto;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.PostCreateDto;
import ru.practicum.dto.PostDto;
import ru.practicum.dto.PostUpdateDto;
import ru.practicum.dto.PostsPageDto;
import ru.practicum.model.Comment;
import ru.practicum.model.Post;
import ru.practicum.model.Tag;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PostServiceImplIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Long testPostId;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных.
        Post post = Post.builder()
                .title("Integration Test Post")
                .text("Some text for integration test")
                .likesCount(0)
                .build();
        Post saved = postRepository.save(post);
        testPostId = saved.getId();

        tagRepository.save(Tag.builder().postId(testPostId).name("initialTag").build());
    }

    @Test
    void createPost_shouldPersistPostAndTagsToDb() {
        PostCreateDto request = new PostCreateDto("New DB Post", "New DB Text", List.of("dbTag1", "#dbTag2"));

        PostDto result = postService.createPost(request);

        assertNotNull(result.id());
        assertEquals("New DB Post", result.title());

        // Проверяем, что теги сохранились (с учетом нормализации, убравшей "#")
        List<Tag> savedTags = tagRepository.findAll();
        assertTrue(savedTags.stream().anyMatch(t -> t.getName().equals("dbTag1")));
        assertTrue(savedTags.stream().anyMatch(t -> t.getName().equals("dbTag2")));
    }

    @Test
    void getPostsPage_shouldReturnPaginatedResultsWithSearch() {
        postRepository.save(Post.builder().title("Another Post about Java").text("Java is great").likesCount(0).build());
        postRepository.save(Post.builder().title("Third Post").text("More Java content").likesCount(0).build());

        PostsPageDto page1 = postService.getPostsPage("Java", 1, 1);

        assertEquals(1, page1.posts().size());
        assertTrue(page1.posts().getFirst().title().contains("Java")
                || page1.posts().getFirst().text().contains("Java"));
        assertTrue(page1.hasNext());
        assertFalse(page1.hasPrev());
        assertEquals(2, page1.lastPage());
    }

    @Test
    void updatePost_shouldUpdateFieldsAndReplaceTags() {
        PostUpdateDto request = new PostUpdateDto(testPostId, "Updated Title", "Updated Text", List.of("newTag"));

        PostDto result = postService.updatePost(request);

        assertEquals("Updated Title", result.title());
        assertEquals("Updated Text", result.text());

        // Проверяем, что старый тег удален, а новый добавлен
        List<Tag> currentTags = tagRepository.findAll();
        assertFalse(currentTags.stream().anyMatch(t -> t.getName().equals("initialTag")));
        assertTrue(currentTags.stream().anyMatch(t -> t.getName().equals("newTag")));
        assertEquals(1, currentTags.size());
    }

    @Test
    void incrementLikes_shouldIncreaseCountInDb() {
        long initialLikes = postService.getPost(testPostId).likesCount();

        long newLikes = postService.incrementLikes(testPostId);

        assertEquals(initialLikes + 1, newLikes);

        Post dbPost = postRepository.findById(testPostId).orElseThrow();
        assertEquals(initialLikes + 1, dbPost.getLikesCount());
    }

    @Test
    void createComment_and_getComments_shouldWorkCorrectly() {
        CommentCreateDto request = new CommentCreateDto(testPostId, "Great post!");

        CommentDto created = postService.createComment(testPostId, request);

        assertNotNull(created.id());
        assertEquals("Great post!", created.text());

        List<CommentDto> comments = postService.getCommentsByPostId(testPostId);
        assertEquals(1, comments.size());
        assertEquals("Great post!", comments.getFirst().text());
    }

    @Test
    void deleteComment_shouldRemoveFromDb() {
        Comment comment = commentRepository.save(Comment.builder()
                .postId(testPostId)
                .text("To be deleted")
                .build());

        postService.deleteComment(testPostId, comment.getId());

        assertFalse(commentRepository.findById(comment.getId()).isPresent());
    }

    @Test
    void getCommentById_wrongPostId_shouldThrowNotFound() {
        Comment comment = commentRepository.save(Comment.builder()
                .postId(testPostId)
                .text("Test comment")
                .build());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> postService.getCommentById(999L, comment.getId()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}