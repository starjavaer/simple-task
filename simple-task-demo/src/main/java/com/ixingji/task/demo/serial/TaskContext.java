package com.ixingji.task.demo.serial;

import lombok.Getter;
import lombok.Setter;

public class TaskContext {

    private static ThreadLocal<TaskContext> contextThreadLocal = new ThreadLocal<>();

    @Getter
    @Setter
    private Integer currentSubTaskId;

    @Getter
    @Setter
    private String storage;

    public static TaskContext get() {
        if (contextThreadLocal.get() == null) {
            TaskContext taskContext = new TaskContext();
            contextThreadLocal.set(taskContext);
        }
        return contextThreadLocal.get();
    }

    public static void remove() {
        contextThreadLocal.remove();
    }

}
