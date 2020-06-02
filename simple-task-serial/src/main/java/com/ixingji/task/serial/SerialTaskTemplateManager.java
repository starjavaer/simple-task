package com.ixingji.task.serial;

import com.ixingji.task.serial.model.SerialTaskTemplate;

import java.util.HashMap;
import java.util.Map;

public final class SerialTaskTemplateManager {

    private static final Map<String, SerialTaskTemplate> TASK_TEMPLATE_MAP = new HashMap<>();

    private SerialTaskTemplateManager() {

    }

    public static void register(String taskName, SerialTaskTemplate taskTemplate) {
        TASK_TEMPLATE_MAP.put(taskName, taskTemplate);
    }

    public static SerialTaskTemplate get(String taskName) {
        return TASK_TEMPLATE_MAP.get(taskName);
    }

}
