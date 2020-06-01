package com.ixingji.task.atomic.core;

import com.ixingji.task.atomic.model.AtomicTask;

import java.util.concurrent.LinkedBlockingDeque;

public class AtomicTaskFinishQueue extends LinkedBlockingDeque<AtomicTask> {

    private static final AtomicTaskFinishQueue INSTANCE = new AtomicTaskFinishQueue(100);

    public static AtomicTaskFinishQueue getInstance() {
        return INSTANCE;
    }

    public AtomicTaskFinishQueue(int capacity) {
        super(capacity);
    }

    @Override
    public AtomicTask poll() {
        return super.poll();
    }

    @Override
    public boolean offer(AtomicTask atomicTask) {
        return super.offer(atomicTask);
    }

}
