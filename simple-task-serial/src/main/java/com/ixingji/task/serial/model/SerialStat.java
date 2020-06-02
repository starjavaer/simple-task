package com.ixingji.task.serial.model;

import lombok.Getter;

import java.util.Objects;

public enum SerialStat {

    /**
     * init
     */
    INIT("初始"),

    /**
     * running
     */
    RUNNING("运行中"),

    /**
     * pause
     */
    PAUSE("暂停"),

    /**
     * success
     */
    SUC("成功"),

    /**
     * failed
     */
    FAIL("失败");

    @Getter
    private final String value;

    SerialStat(String value) {
        this.value = value;
    }

    public static SerialStat value(String value){
        if (value == null) {
            return null;
        }

        for (SerialStat serialStat : values()) {
            if (Objects.equals(serialStat.getValue(), value)) {
                return serialStat;
            }
        }

        return null;
    }

}
