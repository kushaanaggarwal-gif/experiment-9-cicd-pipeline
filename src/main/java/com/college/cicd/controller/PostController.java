package com.college.cicd.controller;

import com.college.cicd.model.Post;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {

    private final ConcurrentHashMap<Long, Post> postDatabase = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public PostController() {
        // Seed default sample posts for API integration testing demonstration
        postDatabase.put(1L, new Post(1L, "CI/CD Best Practices", "Automated testing and continuous delivery build engineering trust.", "DevOps Lead"));
        postDatabase.put(2L, new Post(2L, "Canary Deployment Guide", "Canary routing minimizes blast radius during software rollouts.", "SRE Team"));
        postDatabase.put(3L, new Post(3L, "Testing Microservices with Pact", "Consumer-driven contract testing prevents breaking changes across teams.", "QA Specialist"));
        idSequence.set(4);
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(new ArrayList<>(postDatabase.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {
        Post post = postDatabase.get(id);
        if (post != null) {
            return ResponseEntity.ok(post);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        Long id = idSequence.getAndIncrement();
        post.setId(id);
        postDatabase.put(id, post);
        return ResponseEntity.status(201).body(post);
    }
}
