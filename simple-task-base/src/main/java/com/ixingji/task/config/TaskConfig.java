package com.ixingji.task.config;

import lombok.Data;

@Data
public final class TaskConfig {

    private static TaskConfig instance = new TaskConfig();

    /**
     * 当前实例名称
     */

    private String myName;

    /**
     *  zk相关配置
     */

    private boolean useZk;

    private String zkUrl;

    private String taskNodePath;


    public static TaskConfig getInstance() {
        return instance;
    }

    private TaskConfig() {

    }

}
