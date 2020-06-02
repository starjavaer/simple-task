package com.ixingji.task.serial.listener;

import com.ixingji.task.serial.model.SerialTask;

public interface SerialTaskListener {

    void beforeHandle(SerialTask serialTask);

    void afterHandle(SerialTask serialTask);

}
