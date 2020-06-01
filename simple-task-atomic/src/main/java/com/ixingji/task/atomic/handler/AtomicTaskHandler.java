package com.ixingji.task.atomic.handler;

import com.ixingji.task.atomic.model.AtomicTaskData;

public interface AtomicTaskHandler<T> {

    boolean doHandler(AtomicTaskData<T> taskData) throws Exception;

    boolean doRestore(AtomicTaskData<T> taskData) throws Exception;

}
