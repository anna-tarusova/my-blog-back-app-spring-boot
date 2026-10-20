package ru.practicum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyBlogBackAppApplication {

    private static final Logger log = LoggerFactory.getLogger(MyBlogBackAppApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MyBlogBackAppApplication.class, args);

        log.info("Blog backend started: http://localhost:8080/api/posts?search=&pageNumber=1&pageSize=5");
    }
}