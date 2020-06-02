package com.ixingji.task.demo.serial.listener;

import com.ixingji.task.demo.serial.TaskContext;
import com.ixingji.task.demo.serial.dao.SubTaskDao;
import com.ixingji.task.demo.serial.dao.impl.SubTaskDaoImpl;
import com.ixingji.task.demo.serial.model.BusinessSubTask;
import com.ixingji.task.serial.listener.SerialSubTaskListener;
import com.ixingji.task.serial.model.SerialSubTask;

public class SubTaskListener implements SerialSubTaskListener {

    @Override
    public void beforeHandle(SerialSubTask serialSubTask) {
        updateSubTaskState(serialSubTask);
        // 将subTaskId存入TaskContext，用于记录log
        TaskContext.get().setCurrentSubTaskId((Integer) serialSubTask.getAttachment());
    }

    @Override
    public void afterHandle(SerialSubTask serialSubTask) {
        updateSubTaskState(serialSubTask);
    }

    private void updateSubTaskState(SerialSubTask serialSubTask) {
        SubTaskDao subTaskDao = new SubTaskDaoImpl();
        BusinessSubTask businessSubTask = new BusinessSubTask();
        businessSubTask.setId((Integer) serialSubTask.getAttachment());
        businessSubTask.setState(serialSubTask.getStat().getValue());
        subTaskDao.update(businessSubTask);
    }

}
