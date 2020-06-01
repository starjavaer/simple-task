package com.ixingji.task.util;

import com.ixingji.task.exception.TaskException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ZkUtils {

    private static final Map<String, CuratorFramework> CURATOR_MAP = new HashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (CURATOR_MAP.size() != 0) {
                    for (CuratorFramework curator : CURATOR_MAP.values()) {
                        curator.close();
                    }
                }
            }
        }));
    }

    public static CuratorFramework getConnection(String zkUrl) {
        synchronized (ZkUtils.class) {
            if (CURATOR_MAP.get(zkUrl) == null) {
                CURATOR_MAP.put(zkUrl, createConnection(zkUrl));
            }
        }

        return CURATOR_MAP.get(zkUrl);
    }

    private static CuratorFramework createConnection(String zkUrl) {
        CuratorFramework curator = CuratorFrameworkFactory.newClient(
                zkUrl,
                new ExponentialBackoffRetry(100, 6)
        );

        // start connection
        curator.start();

        // wait 3 second to establish connection
        try {
            curator.blockUntilConnected(3, TimeUnit.SECONDS);

            if (curator.getZookeeperClient().isConnected()) {
                return curator;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        curator.close();

        throw new TaskException("failed to connection to zookeeper, url:" + zkUrl);
    }

    public static void addChildPathListener(String zkUrl, String path, PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache childrenCache = new PathChildrenCache(getConnection(zkUrl), path, true);
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        childrenCache.getListenable().addListener(listener);
    }

}
