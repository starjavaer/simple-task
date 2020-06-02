package com.ixingji.task.demo.serial.converter;

import com.ixingji.task.demo.serial.model.BusinessSubTask;
import com.ixingji.task.serial.model.SerialSubTask;

import java.util.Map;
import java.util.stream.Collectors;

public class SubTaskConverter {

    public static BusinessSubTask convert(SerialSubTask serialSubTask) {
        BusinessSubTask businessSubTask = new BusinessSubTask();
        businessSubTask.setName(serialSubTask.getName());
        businessSubTask.setIndex(serialSubTask.getTemplate().getIndex());
        businessSubTask.setState(serialSubTask.getStat().getValue());
        if (serialSubTask.getShardStatMap() != null
                && serialSubTask.getShardStatMap().size() > 0) {
            Map<String, String> shardStatMap = serialSubTask.getShardStatMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            shardStatEntry -> shardStatEntry.getValue().getValue()
                    ));
            businessSubTask.setShardMap(shardStatMap);
        }
        businessSubTask.setParam(String.valueOf(serialSubTask.getParam()));
        return businessSubTask;
    }

}
