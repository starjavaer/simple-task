package com.ixingji.task.atomic.config;

import lombok.Getter;
import lombok.Setter;

public final class AtomicTaskConfig {

    private static AtomicTaskConfig instance = new AtomicTaskConfig();

    @Getter
    @Setter
    private String onlineNodePath;


    private AtomicTaskConfig() {

    }

    public static AtomicTaskConfig getInstance() {
        return instance;
    }

}
