package com.ixingji.task.atomic;

import com.ixingji.task.TaskInitializer;
import com.ixingji.task.atomic.config.AtomicTaskConfig;
import com.ixingji.task.atomic.core.AtomicTaskFinishWorker;
import com.ixingji.task.atomic.handler.AtomicTaskHandler;
import com.ixingji.task.atomic.listener.AtomicTaskListener;
import com.ixingji.task.atomic.util.AtomicTaskPathUtils;
import com.ixingji.task.config.TaskConfig;
import com.ixingji.task.util.ZkUtils;
import org.apache.curator.framework.CuratorFramework;

public final class AtomicTaskInitializer extends TaskInitializer {

    private static final String PROP_ONLINE_NODE_PATH = "onlineNodePath";

    private static AtomicTaskInitializer instance = new AtomicTaskInitializer();

    private volatile static boolean initialized;

    private AtomicTaskInitializer() {

    }

    public <T> void registerTaskHandler(String handlerName, AtomicTaskHandler<T> taskHandler) {
        AtomicTaskHandlerManager.registerHandler(handlerName, taskHandler);
    }

    @Override
    public synchronized void init() throws Exception {
        super.init();

        if (initialized) {
            return;
        }

        // 1.收集配置
        applyConfig(PROP_ONLINE_NODE_PATH, value -> AtomicTaskConfig.getInstance().setOnlineNodePath(value));

        // 2.注册监听
        registerListenerIfNeeded();

        // 3.启动 task finish worker
        AtomicTaskFinishWorker asyncFinishWorker = new AtomicTaskFinishWorker();
        asyncFinishWorker.setDaemon(true);
        asyncFinishWorker.start();

        initialized = true;
    }

    private void registerListenerIfNeeded() throws Exception {
        if (!TaskConfig.getInstance().isUseZk()) {
            return;
        }

        CuratorFramework zkConn = ZkUtils.getConnection(TaskConfig.getInstance().getZkUrl());
        String atomicPath = AtomicTaskPathUtils.atomic();

        if (zkConn.checkExists().forPath(atomicPath) == null) {
            zkConn.create().forPath(atomicPath);
        }

        ZkUtils.addChildPathListener(
                TaskConfig.getInstance().getZkUrl(),
                atomicPath,
                new AtomicTaskListener()
        );
    }

    public static AtomicTaskInitializer getInstance() {
        return instance;
    }

}
