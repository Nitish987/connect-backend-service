package com.conceptune.connect.database.orm;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrmCache {
    private static final Map<Class<?>, Field[]> MODEL_FIELD_MAP_CACHE = new ConcurrentHashMap<>();

    public static Field[] getAndCacheFields(Class<?> clazz) {
        return MODEL_FIELD_MAP_CACHE.computeIfAbsent(clazz, c -> {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
            }
            return fields;
        });
    }
}
