package com.ixingji.task.serial;

import com.ixingji.task.serial.model.SerialTask;

import java.util.HashMap;
import java.util.Map;

public final class SerialTaskManager {

    private static ThreadLocal<Map<String, SerialTask>> taskMapThreadLocal = new ThreadLocal<>();

    private SerialTaskManager() {

    }

    public static boolean register(SerialTask task) {
        Map<String, SerialTask> taskMap = null;
        if ((taskMap = taskMapThreadLocal.get()) == null) {
            taskMapThreadLocal.set(new HashMap<String, SerialTask>());
            taskMap = taskMapThreadLocal.get();
        }
        taskMap.put(task.getName(), task);
        return true;
    }

    public static SerialTask get(String taskName) {
        if (taskMapThreadLocal.get() == null) {
            return null;
        }
        return taskMapThreadLocal.get().get(taskName);
    }

}
