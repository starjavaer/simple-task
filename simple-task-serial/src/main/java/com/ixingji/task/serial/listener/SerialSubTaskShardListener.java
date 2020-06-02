package com.ixingji.task.serial.listener;

import com.ixingji.task.serial.model.SerialSubTask;
import com.ixingji.task.serial.model.SerialTask;

public interface SerialSubTaskShardListener {

    void beforeHandle(SerialSubTask serialSubTask, String shard);

    void afterHandle(SerialSubTask serialSubTask, String shard);

}
