package com.conceptune.connect.threads;

import com.conceptune.connect.database.repository.StoryRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@ConditionalOnProperty(name = "connect.threads.story-removal-thread", havingValue = "true")
public class StoryRemovalThread {

    @Autowired
    private StoryRepository storyRepository;

    @Scheduled(cron = "0 0 12 * * ?")
    public void run() {
        try {
            boolean isDeleted = storyRepository.deleteAllExpired();
            if (isDeleted) {
                log.info("Expired stories deleted successfully.");
            } else {
                log.warn("No expired stories found.");
            }
        } catch (Exception e) {
            log.error("Error while deleting expired stories.", e);
        }
    }
}
