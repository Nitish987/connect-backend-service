package com.conceptune.connect.database.orm;

import com.conceptune.connect.database.template.StorageTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRepository<E> {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected NamedParameterJdbcTemplate npJdbcTemplate;

    @Autowired
    protected StorageTemplate storageTemplate;

    /**
     * Maps table columns to object entity class
     * @param rs Result Set containing table data
     * @param clazz Required Class type
     * @return List of E (Entity) type after mapping
     */
    protected List<E> map(ResultSet rs, Class<E> clazz) {
        try {
            List<E> entities = new ArrayList<E>();
            Field[] fields = InMemoryOrmCache.getAndCacheFields(clazz);

            while (rs.next()) {
                E entity = clazz.getDeclaredConstructor().newInstance();

                for (Field field : fields) {
                    if (field.isAnnotationPresent(MapColumn.class)) {
                        MapColumn annotation = field.getAnnotation(MapColumn.class);
                        String columnName = annotation.name();
                        field.set(entity, rs.getObject(columnName));
                    }
                }

                entities.add(entity);
            }

            return entities;
        } catch (SQLException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            System.out.println(e.getMessage());
            return List.of();
        }
    }
}
