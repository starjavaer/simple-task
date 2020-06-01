package com.ixingji.task;

import com.ixingji.task.model.Task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class TaskManager {

    private static final Map<String, Task> TASK_MAP = new HashMap<>();

    private TaskManager() {

    }

    public static void registerTask(Task task) {
        TASK_MAP.put(task.getName(), task);
    }

    public Collection<Task> getTasks() {
        return TASK_MAP.values();
    }

}
