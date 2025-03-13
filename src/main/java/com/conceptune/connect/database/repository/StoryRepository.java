package com.conceptune.connect.database.repository;

import com.conceptune.connect.database.models.Story;
import com.conceptune.connect.database.orm.AbstractRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Log4j2
@Repository
public class StoryRepository extends AbstractRepository<Story> {

    public boolean save(Story story) throws DataAccessException {
        String sql = "INSERT INTO ct_stories (user_id, type, content, created_at) VALUES (?,?,?,?)";
        int effect = jdbcTemplate.update(sql, story.getUserId(), story.getType(), story.getContent(), story.getCreatedAt());
        return effect > 0;
    }

    /**
     * Finds all user stories in descending order those are within the time frame of 24 hours of the story creation
     * @param userIds List of user-ids
     * @return List of stories
     * @throws DataAccessException if failed to find stories
     */
    public List<Story> findAllWithin24HourTimeFrame(List<String> userIds) throws DataAccessException {
        String sql = "SELECT * FROM ct_stories WHERE user_id IN (:user_ids) AND (created_at + INTERVAL '24 hours') > NOW() ORDER BY created_at DESC";
        return npJdbcTemplate.query(sql, Map.of("user_ids", userIds), rs -> {
            return map(rs, Story.class);
        });
    }

    public boolean deleteById(Long id) throws DataAccessException {
        String sql = "DELETE FROM ct_stories WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public boolean deleteAllExpired() throws DataAccessException {
        String sql = "DELETE FROM ct_stories WHERE (created_at + INTERVAL '24 hours') < NOW()";
        return jdbcTemplate.update(sql) > 0;
    }
}
