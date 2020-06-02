package com.ixingji.task.serial.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SerialTask {

    @Setter
    private String name;

    @Setter
    private List<SerialSubTask> orderedSubTasks = new ArrayList<>();

    @Setter
    private SerialStat stat = SerialStat.INIT;

    @Setter
    private Object attachment;


    public SerialTask(String name, List<SerialSubTask> orderedSubTasks) {
        this.name = name;
        this.orderedSubTasks = orderedSubTasks;
    }

    public SerialTask(String name) {
        this.name = name;
    }

    public void updateState(SerialStat stat) {
        this.stat = stat;
    }

}
