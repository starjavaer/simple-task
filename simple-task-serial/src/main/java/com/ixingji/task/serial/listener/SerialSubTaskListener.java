package com.ixingji.task.serial.listener;

import com.ixingji.task.serial.model.SerialSubTask;
import com.ixingji.task.serial.model.SerialTask;

public interface SerialSubTaskListener {

    void beforeHandle(SerialSubTask serialSubTask);

    void afterHandle(SerialSubTask serialSubTask);

}
