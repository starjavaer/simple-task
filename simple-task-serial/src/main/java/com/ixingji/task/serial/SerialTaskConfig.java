package com.ixingji.task.serial;

import lombok.Getter;
import lombok.Setter;

@Getter
public final class SerialTaskConfig {

    private static final SerialTaskConfig INSTANCE = new SerialTaskConfig();

    @Setter
    private String scanPackage;


    public static SerialTaskConfig getInstance() {
        return INSTANCE;
    }

    private SerialTaskConfig() {

    }

}

