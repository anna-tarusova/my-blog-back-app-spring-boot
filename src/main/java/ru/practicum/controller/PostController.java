package ru.practicum.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.practicum.dto.*;
import ru.practicum.service.PostService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/api/posts", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(
        origins = {"http://localhost", "http://localhost:80", "http://localhost:3000", "http://localhost:5173"},
        allowCredentials = "true",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS,
                RequestMethod.PATCH}
)
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public PostsPageDto getPosts(@RequestParam(required = false) String search,
                                 @RequestParam(defaultValue = "1") int pageNumber,
                                 @RequestParam(defaultValue = "10") int pageSize) {
        return postService.getPostsPage(search, pageNumber, pageSize);
    }

    @GetMapping("/{id}")
    public PostDto getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        byte[] image = postService.getImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(image);
    }

    // 1. Валидация при создании поста
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostCreateDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
    }

    // 2. Валидация при обновлении поста
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @Valid @RequestBody PostUpdateDto request) {
        return ResponseEntity.ok(postService.updatePost(request));
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateImage(@PathVariable Long id, @RequestPart("image") MultipartFile image)
            throws IOException {
        postService.updateImage(id, image.getBytes());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Long> incrementLikes(@PathVariable Long id) {
        return ResponseEntity.ok(postService.incrementLikes(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getCommentsByPostId(id));
    }

    @GetMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDto> getComment(@PathVariable Long postId, @PathVariable Long commentId) {
        return ResponseEntity.ok(postService.getCommentById(postId, commentId));
    }

    // 3. Валидация при создании комментария
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(@PathVariable Long postId, @Valid @RequestBody CommentCreateDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createComment(postId, request));
    }

    // 4. Валидация при обновлении комментария
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long postId, @PathVariable Long commentId, @Valid @RequestBody CommentUpdateDto request) {
        return ResponseEntity.ok(postService.updateComment(postId, commentId, request));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        postService.deleteComment(postId, commentId);
        return ResponseEntity.ok().build();
    }
}