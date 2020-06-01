package com.ixingji.task.atomic;

import com.ixingji.task.atomic.handler.AtomicTaskHandler;

import java.util.HashMap;
import java.util.Map;

public final class AtomicTaskHandlerManager {

    private static final Map<String, AtomicTaskHandler<?>> TASK_HANDLER_MAP = new HashMap<>();

    private AtomicTaskHandlerManager() {

    }

    public static void registerHandler(String handlerName, AtomicTaskHandler<?> taskHandler) {
        TASK_HANDLER_MAP.put(handlerName, taskHandler);
    }

    public static AtomicTaskHandler<?> getHandler(String handlerName) {
        return TASK_HANDLER_MAP.get(handlerName);
    }

}
