package com.ixingji.task.serial.model;

import lombok.Getter;

public enum SerialStat {

    INIT("初始"),

    RUNNING("运行中"),

    PAUSE("暂停"),

    SUC("成功"),

    FAIL("失败");

    @Getter
    private String value;

    SerialStat(String value) {
        this.value = value;
    }

}
