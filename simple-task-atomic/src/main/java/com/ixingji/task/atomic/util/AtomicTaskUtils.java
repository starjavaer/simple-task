package com.ixingji.task.atomic.util;

import com.ixingji.task.atomic.AtomicTaskManager;
import com.ixingji.task.atomic.handler.AtomicTaskHandler;
import com.ixingji.task.atomic.model.AtomicTask;

public class AtomicTaskUtils {

    public static <T> AtomicTask registerTask(String taskName, AtomicTaskHandler<T> taskHandler, boolean distributed) {
        AtomicTask atomicTask = new AtomicTask(taskName);
        atomicTask.setHandler(taskHandler);
        atomicTask.setDistributed(distributed);
        AtomicTaskManager.registerTask(atomicTask);
        return atomicTask;
    }

    public static <T> AtomicTask appendTask(String taskName, AtomicTaskHandler<T> taskHandler, boolean distributed) {
        AtomicTask lastTask = new AtomicTask(taskName);
        lastTask.setHandler(taskHandler);
        lastTask.setDistributed(distributed);

        AtomicTask atomicTask = AtomicTaskManager.getTask(taskName);
        lastTask.setPrev(atomicTask);

        AtomicTaskManager.registerTask(atomicTask);
        return lastTask;
    }

}
