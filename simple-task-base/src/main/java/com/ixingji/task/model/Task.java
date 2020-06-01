package com.ixingji.task.model;

import com.ixingji.task.TaskManager;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Task {

    private String name;

    @Setter
    private Object attachment;

    public Task(String name) {
        this.name = name;
        TaskManager.registerTask(this);
    }

}
