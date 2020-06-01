package com.ixingji.task.atomic.core;

import com.alibaba.fastjson.JSON;
import com.ixingji.task.atomic.config.AtomicTaskConfig;
import com.ixingji.task.atomic.model.AtomicTask;
import com.ixingji.task.atomic.model.AtomicTaskAction;
import com.ixingji.task.atomic.model.AtomicTaskData;
import com.ixingji.task.atomic.util.AtomicTaskPathUtils;
import com.ixingji.task.config.TaskConfig;
import com.ixingji.task.util.ZkUtils;
import lombok.Getter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class AtomicTaskActionLooper {

    private final String taskName;

    private final AtomicTaskAction taskAction;

    @Getter
    private final Map<String, AtomicTaskData<?>> resultDataMap = new HashMap<>();

    public AtomicTaskActionLooper(String taskName, AtomicTaskAction taskAction) {
        this.taskName = taskName;
        this.taskAction = taskAction;
    }

    public void start() throws Exception {
        String actionPath = AtomicTaskPathUtils.taskAction(taskName, taskAction);

        CuratorFramework zkConn = ZkUtils.getConnection(TaskConfig.getInstance().getZkUrl());

        while (true) {
            List<String> doneNodes = zkConn.getChildren().forPath(actionPath);
            List<String> onlineNodes = zkConn.getChildren().forPath(AtomicTaskConfig.getInstance().getOnlineNodePath());

            if (doneNodes.size() >= onlineNodes.size()) {
                for (String doneNode : doneNodes) {
                    byte[] dataBytes = zkConn.getData().forPath(ZKPaths.makePath(actionPath, doneNode));
                    String nodeData = new String(dataBytes, StandardCharsets.UTF_8);
                    AtomicTaskData<?> resultData = JSON.parseObject(nodeData, AtomicTaskData.class);

                    resultDataMap.put(doneNode, resultData);
                }
                break;
            }

            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
    }

    public Map<AtomicTask.Stat, List<String>> getStat2NodesMap() {
        Map<AtomicTask.Stat, List<String>> stat2NodesMap = new HashMap<>();
        for (Map.Entry<String, AtomicTaskData<?>> resultDataEntry : resultDataMap.entrySet()) {
            AtomicTask.Stat taskStat = resultDataEntry.getValue().getTaskStat();
            stat2NodesMap.computeIfAbsent(taskStat, k -> new ArrayList<>());
            stat2NodesMap.get(taskStat).add(resultDataEntry.getKey());
        }
        return stat2NodesMap;
    }

}
