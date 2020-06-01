package com.ixingji.task.atomic.util;

import com.ixingji.task.atomic.constant.AtomicTaskConstants;
import com.ixingji.task.atomic.model.AtomicTaskAction;
import com.ixingji.task.config.TaskConfig;
import com.ixingji.task.util.TaskPathUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.server.ZKDatabase;

public class AtomicTaskPathUtils {

    public static String atomic() {
        return ZKPaths.makePath(TaskConfig.getInstance().getTaskNodePath(), AtomicTaskConstants.FRAMEWORK_NAME.toUpperCase());
    }

    public static String taskAction(String taskName, AtomicTaskAction taskAction) {
        return ZKPaths.makePath(TaskPathUtils.task(taskName), taskAction.name());
    }

    public static String myNode(String taskName, AtomicTaskAction handle) {
        return ZKPaths.makePath(taskAction(taskName, handle), TaskConfig.getInstance().getMyName());
    }

}
