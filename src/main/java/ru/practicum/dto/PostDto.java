package ru.practicum.dto;

import java.util.List;

public record PostDto(
        Long id,
        String title,
        String text,
        List<String> tags,
        long likesCount,
        long commentsCount
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String title;
        private String text;
        private List<String> tags;
        private long likesCount;
        private long commentsCount;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder likesCount(long likesCount) {
            this.likesCount = likesCount;
            return this;
        }

        public Builder commentsCount(long commentsCount) {
            this.commentsCount = commentsCount;
            return this;
        }

        public PostDto build() {
            return new PostDto(id, title, text, tags, likesCount, commentsCount);
        }
    }
}