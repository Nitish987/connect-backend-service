package com.conceptune.connect.controllers;

import com.conceptune.connect.database.models.Story;
import com.conceptune.connect.dto.request.AddStory;
import com.conceptune.connect.dto.request.UserIds;
import com.conceptune.connect.services.StoryService;
import com.conceptune.connect.utils.Response;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/story")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @PostMapping("/add")
    public ResponseEntity<Response<?>> addStory(Authentication auth, @RequestBody  @Valid AddStory addStory) {
        Response<?> response = storyService.addStory(auth.getPrincipal().toString(), addStory);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/list")
    public ResponseEntity<Response<List<Story>>> listStory(@RequestBody  @Valid UserIds userIds) {
        List<Story> stories = storyService.listStory(userIds);
        return ResponseEntity.ok(Response.success("Stories", stories));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Response<?>> deleteStory(@RequestParam Long storyId) {
        Response<?> response = storyService.deleteStory(storyId);
        return ResponseEntity.ok(response);
    }
}
