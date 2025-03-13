package com.conceptune.connect.services;

import com.conceptune.connect.database.models.Story;
import com.conceptune.connect.database.repository.StoryRepository;
import com.conceptune.connect.dto.request.AddStory;
import com.conceptune.connect.dto.request.UserIds;
import com.conceptune.connect.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class StoryService {

    @Autowired
    private StoryRepository storyRepository;

    public Response<?> addStory(String userId, AddStory addStory) {
        Story story = new Story();
        story.setUserId(userId);
        story.setType(addStory.getType());
        story.setContent(addStory.getContent());
        story.setCreatedAt(Timestamp.from(Instant.now()));

        boolean isStoryAdded = storyRepository.save(story);

        if (isStoryAdded) {
            return Response.success("Story added successfully.");
        }

        return Response.error("Unable to add story.");
    }

    public List<Story> listStory(UserIds userIds) {
        return storyRepository.findAllWithin24HourTimeFrame(userIds.getUserIds());
    }

    public Response<?> deleteStory(Long storyId) {
        boolean isStoryDeleted = storyRepository.deleteById(storyId);

        if (isStoryDeleted) {
            return Response.success("Stories deleted successfully.");
        }

        return Response.error("Unable to delete stories.");
    }
}
