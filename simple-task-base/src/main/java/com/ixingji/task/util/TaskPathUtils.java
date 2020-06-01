package com.ixingji.task.util;

import com.ixingji.task.config.TaskConfig;
import org.apache.curator.utils.ZKPaths;

public class TaskPathUtils {

    public static String task(String taskName) {
        return ZKPaths.makePath(TaskConfig.getInstance().getTaskNodePath(), taskName);
    }

}
