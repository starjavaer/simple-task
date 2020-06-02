package com.ixingji.task.demo.serial.converter;

import com.ixingji.task.demo.serial.model.BusinessSubTask;
import com.ixingji.task.demo.serial.model.BusinessTask;
import com.ixingji.task.serial.model.SerialSubTask;
import com.ixingji.task.serial.model.SerialTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskConverter {

    public static BusinessTask convert(SerialTask serialTask) {
        BusinessTask businessTask = new BusinessTask();
        businessTask.setName(serialTask.getName());
        businessTask.setState(serialTask.getStat().getValue());
        if (serialTask.getOrderedSubTasks() != null && serialTask.getOrderedSubTasks().size() > 0) {
            List<BusinessSubTask> businessSubTasks = new ArrayList<>();
            for (SerialSubTask serialSubTask : serialTask.getOrderedSubTasks()) {
                BusinessSubTask businessSubTask = new BusinessSubTask();
                businessSubTask.setName(serialSubTask.getName());
                businessSubTask.setIndex(serialSubTask.getTemplate().getIndex());
                businessSubTask.setState(serialSubTask.getStat().getValue());
                businessSubTasks.add(businessSubTask);
            }
            businessTask.setSubTasks(businessSubTasks);
        }
        return businessTask;
    }

}
