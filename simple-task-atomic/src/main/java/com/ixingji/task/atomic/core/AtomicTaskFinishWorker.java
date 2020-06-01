package com.ixingji.task.atomic.core;

import com.ixingji.task.atomic.AtomicTaskExecutor;
import com.ixingji.task.atomic.model.AtomicTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AtomicTaskFinishWorker extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtomicTaskFinishWorker.class);

    @Override
    public void run() {
        AtomicTask atomicTask;
        while ((atomicTask = AtomicTaskFinishQueue.getInstance().poll()) != null) {
            try {
                AtomicTaskExecutor.finish(atomicTask.getName());
            } catch (Exception e) {
                LOGGER.error("finish task error, task: {}", atomicTask, e);
            }
        }
    }

}
