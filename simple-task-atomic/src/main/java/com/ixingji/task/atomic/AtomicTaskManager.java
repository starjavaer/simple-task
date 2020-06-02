package com.ixingji.task.atomic;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ixingji.task.atomic.model.AtomicTask;

import java.util.concurrent.TimeUnit;

public final class AtomicTaskManager {

    private static final Cache<String, AtomicTask> TASK_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(600, TimeUnit.SECONDS).build();

    public static AtomicTask get(String taskName) {
        return TASK_CACHE.getIfPresent(taskName);
    }

    public static void register(AtomicTask task) {
        TASK_CACHE.put(task.getName(), task);
    }

    public static void unregister(String taskName) {
        TASK_CACHE.invalidate(taskName);
    }

}
