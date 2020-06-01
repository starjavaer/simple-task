package com.ixingji.task.atomic.listener;

import com.ixingji.task.atomic.AtomicTaskHandlerManager;
import com.ixingji.task.atomic.AtomicTaskManager;
import com.ixingji.task.atomic.exception.AtomicTaskFailedException;
import com.ixingji.task.atomic.handler.AtomicTaskHandler;
import com.ixingji.task.atomic.model.AtomicTask;
import com.ixingji.task.atomic.model.AtomicTaskAction;
import com.ixingji.task.atomic.model.AtomicTaskData;
import com.ixingji.task.atomic.util.AtomicTaskPathUtils;
import com.ixingji.task.atomic.util.JSONUtils;
import com.ixingji.task.config.TaskConfig;
import com.ixingji.task.util.ZkUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AtomicTaskListener implements PathChildrenCacheListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtomicTaskListener.class);

    @Override
    public void childEvent(CuratorFramework curator, PathChildrenCacheEvent event) throws Exception {
        ChildData eventData = event.getData();
        if (eventData == null) {
            return;
        }

        AtomicTaskData<?> taskData = JSONUtils.parseObj(eventData.getData(), AtomicTaskData.class);

        String taskPath = eventData.getPath();
        ZKPaths.PathAndNode taskPathAndNode = ZKPaths.getPathAndNode(taskPath);

        String taskName = taskPathAndNode.getNode();
        taskData.setTaskName(taskName);

        AtomicTaskAction taskAction = taskData.getTaskAction();

        boolean doSuccess = true;
        boolean hasErr = false;

        CuratorFramework zkConn = ZkUtils.getConnection(TaskConfig.getInstance().getZkUrl());

        switch (event.getType()) {
            case CHILD_ADDED:
                AtomicTaskHandler taskHandler = AtomicTaskHandlerManager.getHandler(taskData.getHandlerName());

                AtomicTask atomicTask;
                if ((atomicTask = AtomicTaskManager.getTask(taskName)) == null
                        || !Objects.equals(atomicTask.getOwner(), AtomicTask.Owner.SENDER)) {
                    atomicTask = new AtomicTask(taskName, AtomicTask.Owner.PARTER);
                    atomicTask.setHandler(taskHandler);
                    AtomicTaskManager.registerTask(atomicTask);
                }

                String myNode = AtomicTaskPathUtils.myNode(taskName, AtomicTaskAction.HANDLE);

                // 正常情况下应该只有 handle suc、restore suc 和 restore fail 三种状态
                // 没有 handle fail
                AtomicTask.Stat taskStat = AtomicTask.Stat.HANDLE_SUC;

                try {
                    atomicTask.updateStatus(AtomicTask.Stat.HANDLING);
                    doSuccess = taskHandler.doHandler(taskData);
                } catch (AtomicTaskFailedException e) {
                    // 出现这个错误，说明任务链中至少存在2个任务
                    taskStat = AtomicTask.Stat.RESTORE_FAIL;
                    atomicTask.updateStatus(taskStat);
                    LOGGER.error("handle task failed, ", e);
                    throw e;
                } catch (Exception e) {
                    hasErr = true;
                    LOGGER.error("handle task error, ", e);
                    throw new AtomicTaskFailedException(e);
                } finally {
                    if (hasErr || !doSuccess) {
                        // 如果代码执行到这里，说明任务链中只有一个任务存在
                        taskStat = AtomicTask.Stat.HANDLE_FAIL;
                        atomicTask.updateStatus(taskStat);
                    }

                    zkConn.create().forPath(myNode, JSONUtils.toStrBytes(new AtomicTaskData<>(null, taskStat)));
                }
                break;
            case CHILD_UPDATED:
                atomicTask = AtomicTaskManager.getTask(taskName);

                taskHandler = atomicTask.getHandler();

                switch (taskAction) {
                    case HANDLE:
                        break;
                    case RESTORE:
                        myNode = AtomicTaskPathUtils.myNode(taskName, AtomicTaskAction.RESTORE);

                        while (atomicTask != null) {
                            try {
                                atomicTask.updateStatus(AtomicTask.Stat.RESTORING);
                                doSuccess = taskHandler.doRestore(taskData);
                            } catch (Exception e) {
                                hasErr = true;
                                LOGGER.error("restore task error, ", e);
                                throw new AtomicTaskFailedException(e);
                            } finally {
                                taskStat = doSuccess && !hasErr
                                        ? AtomicTask.Stat.RESTORE_SUC : AtomicTask.Stat.RESTORE_FAIL;
                                atomicTask.updateStatus(taskStat);
                                if (hasErr) {
                                    zkConn.create().forPath(myNode, JSONUtils.toStrBytes(new AtomicTaskData<>(null, AtomicTask.Stat.RESTORE_FAIL)));
                                }
                            }

                            atomicTask = atomicTask.getPrev();
                        }

                        zkConn.create().forPath(myNode, JSONUtils.toStrBytes(new AtomicTaskData<>(null, AtomicTask.Stat.RESTORE_SUC)));
                        break;
                    case FINISH:
                        myNode = AtomicTaskPathUtils.myNode(taskName, AtomicTaskAction.FINISH);

                        try {
                            AtomicTaskManager.unregisterTask(taskName);
                        } catch (Exception e) {
                            hasErr = true;
                            LOGGER.error("finish error, ", e);
                        }

                        zkConn.create().forPath(myNode,
                                JSONUtils.toStrBytes(
                                        new AtomicTaskData<>(
                                                null,
                                                !hasErr ? AtomicTask.Stat.FINISH_SUC : AtomicTask.Stat.FINISH_FAIL
                                        )
                                )
                        );
                        break;
                }
            default:
                break;
        }

    }

}
