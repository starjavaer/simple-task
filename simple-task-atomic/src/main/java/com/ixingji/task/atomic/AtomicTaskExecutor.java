package com.ixingji.task.atomic;

import com.ixingji.task.atomic.core.AtomicTaskActionLooper;
import com.ixingji.task.atomic.core.AtomicTaskFinishQueue;
import com.ixingji.task.atomic.exception.AtomicTaskFailedException;
import com.ixingji.task.atomic.handler.AtomicTaskHandler;
import com.ixingji.task.atomic.model.AtomicTask;
import com.ixingji.task.atomic.model.AtomicTaskAction;
import com.ixingji.task.atomic.model.AtomicTaskData;
import com.ixingji.task.atomic.util.AtomicTaskPathUtils;
import com.ixingji.task.atomic.util.AtomicTaskUtils;
import com.ixingji.task.atomic.util.JSONUtils;
import com.ixingji.task.config.TaskConfig;
import com.ixingji.task.exception.TaskNotFoundException;
import com.ixingji.task.util.TaskPathUtils;
import com.ixingji.task.util.ZkUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class AtomicTaskExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtomicTaskExecutor.class);

    public static <T> boolean execute(String taskName, T data, AtomicTaskHandler<T> taskHandler) throws Exception {
        AtomicTask atomicTask = AtomicTaskManager.get(taskName);
        if (atomicTask == null) {
            // task链上第一个task
            atomicTask = AtomicTaskUtils.registerTask(taskName, taskHandler, false);
        } else {
            atomicTask = AtomicTaskUtils.appendTask(taskName, taskHandler, false);
        }

        AtomicTaskData<T> taskData = new AtomicTaskData<>(data);

        boolean doSuccess = true;
        boolean hasErr = false;
        try {
            atomicTask.updateStatus(AtomicTask.Stat.HANDLING);
            doSuccess = taskHandler.doHandler(taskData);
            atomicTask.updateStatus(doSuccess ? AtomicTask.Stat.HANDLE_SUC : AtomicTask.Stat.HANDLE_FAIL);
        } catch (Exception e) {
            hasErr = true;
            LOGGER.error("handle task error", e);
            throw e;
        } finally {
            if (!doSuccess || hasErr) {
                restore(taskName, data);
            }
        }

        return doSuccess;
    }

    public static <T> boolean distribute(String taskName, T data, String handlerName) throws Exception {

        AtomicTask atomicTask = AtomicTaskManager.get(taskName);

        AtomicTaskHandler<?> taskHandler = AtomicTaskHandlerManager.getHandler(handlerName);
        if (atomicTask == null) {
            atomicTask = AtomicTaskUtils.registerTask(taskName, taskHandler, true);
        } else {
            // 限制parter创建的任务不能执行distribute
            if (atomicTask.getOwner().equals(AtomicTask.Owner.PARTER)) {
                throw new IllegalStateException("parter not allow do distribute");
            }
            atomicTask = AtomicTaskUtils.appendTask(taskName, taskHandler, true);
        }

        atomicTask.updateStatus(AtomicTask.Stat.HANDLING);

        AtomicTaskData<T> taskData = new AtomicTaskData<>(data);
        taskData.setHandlerName(handlerName);
        taskData.setTaskAction(AtomicTaskAction.HANDLE);

        String taskPath = TaskPathUtils.task(taskName);
        String handlePath = AtomicTaskPathUtils.taskAction(taskName, AtomicTaskAction.HANDLE);

        CuratorFramework zkConn = ZkUtils.getConnection(TaskConfig.getInstance().getZkUrl());

        // todo 应该创建顺序节点，因为任务主干上可能多次执行distribute
        zkConn.create().forPath(taskPath, JSONUtils.toStrBytes(taskData));
        zkConn.create().forPath(handlePath);

        AtomicTaskActionLooper actionLooper = new AtomicTaskActionLooper(taskName, AtomicTaskAction.HANDLE);
        actionLooper.start();

        Map<AtomicTask.Stat, List<String>> stat2NodesMap = actionLooper.getStat2NodesMap();

        List<String> failedRestoreNodes = stat2NodesMap.get(AtomicTask.Stat.RESTORE_FAIL);
        if (failedRestoreNodes != null && failedRestoreNodes.size() > 0) {
            atomicTask.updateStatus(AtomicTask.Stat.RESTORE_FAIL);
            throw new AtomicTaskFailedException("restore failed, nodes: " + failedRestoreNodes);
        }

        List<String> failedHandleNodes = stat2NodesMap.get(AtomicTask.Stat.HANDLE_FAIL);
        if (failedHandleNodes != null && failedHandleNodes.size() > 0) {
            restore(taskName, data);
        } else {
            atomicTask.updateStatus(AtomicTask.Stat.HANDLE_SUC);
        }

        return failedHandleNodes == null || failedHandleNodes.size() == 0;
    }

    public static <T> boolean restore(String taskName, T data) throws Exception {
        AtomicTask atomicTask = AtomicTaskManager.get(taskName);

        if (atomicTask == null) {
            throw new TaskNotFoundException("none task, name: " + taskName);
        }

        // 从当前任务逆向restore
        while (atomicTask != null) {
            atomicTask.updateStatus(AtomicTask.Stat.RESTORING);
            // 根据是否是distributed任务做不同处理
            if (!atomicTask.isDistributed()) {
                AtomicTaskHandler<?> taskHandler = atomicTask.getHandler();

                boolean doSuccess = false;
                boolean hasErr = false;
                try {
                    doSuccess = taskHandler.doRestore(null);
                } catch (Exception e) {
                    hasErr = true;
                    LOGGER.error("restore error", e);
                }

                if (hasErr || !doSuccess) {
                    atomicTask.updateStatus(AtomicTask.Stat.RESTORE_FAIL);
                    throw new AtomicTaskFailedException("restore failed");
                }
            } else {
                String taskPath = TaskPathUtils.task(taskName);
                String restorePath = AtomicTaskPathUtils.taskAction(taskName, AtomicTaskAction.RESTORE);

                CuratorFramework zkConn = ZkUtils.getConnection(TaskConfig.getInstance().getZkUrl());

                AtomicTaskData<T> taskData = new AtomicTaskData<>(data);
                taskData.setTaskAction(AtomicTaskAction.RESTORE);

                zkConn.setData().forPath(taskPath, JSONUtils.toStrBytes(taskData));
                zkConn.create().forPath(restorePath);

                AtomicTaskActionLooper actionLooper = new AtomicTaskActionLooper(taskName, AtomicTaskAction.RESTORE);
                actionLooper.start();

                Map<AtomicTask.Stat, List<String>> stat2NodesMap = actionLooper.getStat2NodesMap();

                List<String> failedRestoreNodes = stat2NodesMap.get(AtomicTask.Stat.RESTORE_FAIL);
                if (failedRestoreNodes != null && failedRestoreNodes.size() > 0) {
                    atomicTask.updateStatus(AtomicTask.Stat.RESTORE_FAIL);
                    throw new AtomicTaskFailedException("restore failed, nodes: " + failedRestoreNodes);
                }
            }

            atomicTask.updateStatus(AtomicTask.Stat.FINISH_SUC);
            atomicTask = atomicTask.getPrev();
        }

        return true;
    }

    public static void finishAsync(String taskName) {
        AtomicTask atomicTask = AtomicTaskManager.get(taskName);
        AtomicTaskFinishQueue.getInstance().offer(atomicTask);
    }

    public static void finish(String taskName) throws Exception {
        AtomicTask atomicTask = AtomicTaskManager.get(taskName);

        // 判断任务链中是否包含分发类任务
        boolean containsDistributed = false;
        while (atomicTask != null) {
            boolean isDistributed = atomicTask.isDistributed();
            if (isDistributed) {
                containsDistributed = true;
                break;
            }
            atomicTask = atomicTask.getPrev();
        }

        if (!containsDistributed) {
            AtomicTaskManager.unregister(taskName);
        } else {
            String taskPath = TaskPathUtils.task(taskName);
            String finishPath = AtomicTaskPathUtils.taskAction(taskName, AtomicTaskAction.FINISH);
            CuratorFramework zkConn = ZkUtils.getConnection(TaskConfig.getInstance().getZkUrl());
            AtomicTaskData<?> taskData = new AtomicTaskData<>(null);
            taskData.setTaskAction(AtomicTaskAction.FINISH);

            zkConn.create().forPath(finishPath);
            zkConn.setData().forPath(taskPath, JSONUtils.toStrBytes(taskData));

            AtomicTaskActionLooper actionLooper = new AtomicTaskActionLooper(taskName, AtomicTaskAction.FINISH);
            actionLooper.start();

            Map<AtomicTask.Stat, List<String>> stat2NodesMap = actionLooper.getStat2NodesMap();

            List<String> failedFinishNodes = stat2NodesMap.get(AtomicTask.Stat.FINISH_FAIL);

            if (failedFinishNodes != null && failedFinishNodes.size() > 0) {
                throw new AtomicTaskFailedException("finish failed, failed nodes: " + failedFinishNodes);
            }

            zkConn.delete().deletingChildrenIfNeeded().forPath(taskPath);
        }
    }

}
