package com.ixingji.task;

import com.ixingji.task.config.ConfigApplyHandler;
import com.ixingji.task.config.TaskConfig;
import com.ixingji.task.exception.ConfigDuplicateException;
import com.ixingji.task.exception.ConfigNotFoundException;
import com.ixingji.task.util.ZkUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TaskInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskInitializer.class);

    private static final String TASK_CONFIG_FILE = "/task.properties";

    private static final String PROP_MY_NAME = "myName";
    private static final String PROP_USE_ZK = "useZk";
    private static final String PROP_ZK_URL = "zkUrl";
    private static final String PROP_TASK_NODE_PATH = "taskNodePath";

    private volatile static boolean initialized;

    private volatile static Map<String, String> taskConfigs;

    private static void loadConfigs() {
        if (taskConfigs == null) {
            taskConfigs = new HashMap<>();
        }

        Properties taskProps = loadTaskProps();

        if (taskProps == null) {
            return;
        }

        Enumeration<?> enumeration = taskProps.propertyNames();
        while (enumeration.hasMoreElements()) {
            String propName = enumeration.nextElement().toString();
            String propValue = taskProps.getProperty(propName);

            if (taskConfigs.get(propName) == null) {
                throw new ConfigDuplicateException("config '" + propName + "' already exists");
            }

            taskConfigs.put(propName, propValue);
        }
    }

    public synchronized void init() throws Exception {
        if (initialized) {
            return;
        }

        // load config
        loadConfigs();

        applyConfig(PROP_MY_NAME, value -> TaskConfig.getInstance().setMyName(value));

        applyConfig(PROP_USE_ZK, value -> TaskConfig.getInstance().setUseZk("true".equals(value)));

        if (TaskConfig.getInstance().isUseZk()) {
            applyConfig(PROP_ZK_URL, value -> TaskConfig.getInstance().setZkUrl(value));

            applyConfig(PROP_TASK_NODE_PATH, value -> TaskConfig.getInstance().setTaskNodePath(value));

            // create task zk node
            createTaskNodePathIfNeeded();
        }

        initialized = true;
    }

    private void createTaskNodePathIfNeeded() throws Exception {
        if (!TaskConfig.getInstance().isUseZk()) {
            return;
        }

        CuratorFramework zkConn = ZkUtils.getConnection(TaskConfig.getInstance().getZkUrl());

        // create task node
        if (zkConn.checkExists().forPath(TaskConfig.getInstance().getTaskNodePath()) == null) {
            zkConn.create().creatingParentsIfNeeded().forPath(TaskConfig.getInstance().getTaskNodePath());
        }
    }

    protected static void applyConfig(String name, ConfigApplyHandler handler) {
        if (taskConfigs.size() == 0) {
            return;
        }

        String value = taskConfigs.get(name);

        if (StringUtils.isBlank(value)) {
            throw new ConfigNotFoundException("no '" + name + "' configured");
        } else {
            handler.doHandle(value);
        }
    }

    private static Properties loadTaskProps() {
        InputStream is = TaskInitializer.class.getResourceAsStream(TASK_CONFIG_FILE);

        if (is == null) {
            return null;
        }

        Properties props = new Properties();

        InputStreamReader isReader = null;
        try {
            isReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            props.load(isReader);
        } catch (IOException e) {
            LOGGER.error("load config error", e);
        } finally {
            try {
                if (isReader != null) {
                    isReader.close();
                }
            } catch (IOException e) {
                LOGGER.error("close config reader error", e);
            }
        }

        return props;
    }



}
